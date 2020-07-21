(ns quantit.component
  (:require [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]))

(s/def ::deps (s/coll-of keyword? :kind vector?))

(defn class->kw [class]
  (csk/->kebab-case-keyword class))

(defn constr-sym [name]
  (if (some? (namespace name))
    (symbol (namespace name) (str "new-" name))
    (symbol (str "new-" name))))

(defn map-constr-sym [name]
  (if (some? (namespace name))
    (symbol (namespace name) (str "map->" name))
    (symbol (str "map->" name))))


;; TODO: Add default state
;; TODO: Add params and default params
(defmacro defcomponent [name depsv type & body]
  {:pre [(s/valid? symbol? name)
         (s/valid? ::deps depsv)]}
  (let [props (conj (mapv symbol depsv) 'state)
        map-constr (map-constr-sym name)
        constr (constr-sym name)]
    `(do
       (defrecord ~name ~props
         component/Lifecycle
         ~'(start [this] this)
         ~'(stop [this] this))
       (extend-type ~name
         ~type
         ~@body)
       (defn ~constr
         ([] (~map-constr {}))
         ([~'initial-state] (~map-constr ~'initial-state))))))