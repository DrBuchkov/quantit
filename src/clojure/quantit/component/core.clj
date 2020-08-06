(ns quantit.component.core
  (:require [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [quantit.utils :refer [flat-seq->map]]))

(defn constr-sym [name]
  (if (some? (namespace name))
    (symbol (namespace name) (str "new-" name))
    (symbol (str "new-" name))))


(s/fdef constr-sym
        :args (s/cat :name symbol?)
        :ret symbol?)

(defn map-constr-sym [name]
  (if (some? (namespace name))
    (symbol (namespace name) (str "map->" name))
    (symbol (str "map->" name))))

(s/fdef map-constr-sym
        :args (s/cat :name symbol?)
        :ret symbol?)

(defprotocol Component
  (deps [this])
  (deps-kw [this])
  (update-state-before [this bar bar-history])
  (update-state-after [this bar bar-history]))

(def default-update-state-before-form '(update-state-before [this bar bar-history] (:state this)))
(def default-update-state-after-form '(update-state-after [this bar bar-history] (:state this)))

(def comp-methods #{'update-state-before 'update-state-after})

(defn comp-method? [form]
  (some #(= % (first form))
        comp-methods))

(defn update-state-after-implemented? [body]
  (some #(= 'update-state-after (first %)) body))

(defn update-state-before-implemented? [body]
  (some #(= 'update-state-before (first %)) body))

(defmacro defcomponent [name basis comp-type & body]
  {:pre [(s/valid? symbol? name)
         (s/valid? :quantit.component/basis basis)
         (s/valid? :quantit.component/type comp-type)]}
  (let [{:keys [dependencies default-params default-init-state]} (flat-seq->map basis)
        props (conj (mapv symbol dependencies) 'state 'params)
        map-constr (map-constr-sym name)
        constr (constr-sym name)
        dependencies (or dependencies [])
        default-params (or default-params {})
        default-init-state (or default-init-state {})
        type-body (remove comp-method? body)

        comp-body (filter comp-method? body)

        comp-body (if (not (update-state-after-implemented? comp-body))
                    (conj comp-body default-update-state-after-form)
                    comp-body)

        comp-body (if (not (update-state-before-implemented? comp-body))
                    (conj comp-body default-update-state-before-form)
                    comp-body)]
    `(do
       (defrecord ~name ~props
         component/Lifecycle
         (~'start [~'this] (let [~'this (if (some? (:params ~'this))
                                          ~'this
                                          (assoc ~'this :params ~default-params))
                                 ~'this (if (some? (:state ~'this))
                                          ~'this
                                          (assoc ~'this :state ~default-init-state))]
                             ~'this))
         ~'(stop [this] this)
         Component
         (~'deps-kw [~'this] ~dependencies)
         (~'deps [~'this] (mapv #(get ~'this %) (deps-kw ~'this)))
         ~@comp-body

         ~comp-type
         ~@type-body)
       (defn ~constr
         ([] (~map-constr {}))
         ([~'initial-state] (~map-constr ~'initial-state))))))

