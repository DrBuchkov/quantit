(ns quantit.component
  (:require [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [quantit.utils :refer [flat-seq->map]]))

(s/def ::dependencies (s/coll-of keyword? :kind vector?))
(s/def ::default-params map?)
(s/def ::default-init-state map?)
(s/def ::basis-map (s/keys :opt-un [::dependencies ::default-params ::default-init-state]))

(s/def ::basis #(->> % flat-seq->map (s/def ::basis-map)))

(defn constr-sym [name]
  (if (some? (namespace name))
    (symbol (namespace name) (str "new-" name))
    (symbol (str "new-" name))))

(defn map-constr-sym [name]
  (if (some? (namespace name))
    (symbol (namespace name) (str "map->" name))
    (symbol (str "map->" name))))

(defprotocol Component
  (deps [this])
  (deps-kw [this]))

;; TODO: Add default state
;; TODO: Add params and default params
(defmacro defcomponent [name basis type & body]
  {:pre [(s/valid? symbol? name)
         (s/valid? ::basis basis)]}
  (let [{:keys [dependencies default-params default-init-state]} (flat-seq->map basis)
        props (conj (mapv symbol dependencies) 'state 'params)
        map-constr (map-constr-sym name)
        constr (constr-sym name)
        dependencies (or dependencies [])
        default-params (or default-params {})
        default-init-state (or default-init-state {})]
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
         ~type
         ~@body)
       (defn ~constr
         ([] (~map-constr {}))
         ([~'initial-state] (~map-constr ~'initial-state))))))