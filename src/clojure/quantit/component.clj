(ns quantit.component
  (:require [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]))

(s/def ::deps (s/coll-of keyword? :kind vector?))

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
(defmacro defcomponent [name depsv type & body]
  {:pre [(s/valid? symbol? name)
         (s/valid? ::deps depsv)]}
  (let [props (conj (mapv symbol depsv) 'state 'params)
        map-constr (map-constr-sym name)
        constr (constr-sym name)]
    `(do
       (defrecord ~name ~props
         component/Lifecycle
         ~'(start [this] this)
         ~'(stop [this] this)
         Component
         (~'deps-kw [~'this] ~depsv)
         (~'deps [~'this] (mapv #(get ~'this %) (deps-kw ~'this)))
         ~type
         ~@body)
       (defn ~constr
         ([] (~map-constr {}))
         ([~'initial-state] (~map-constr ~'initial-state))))))