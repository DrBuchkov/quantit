(ns quantit.execution
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [quantit.component :refer [constr-sym]]))

(s/def ::dependency-mappings (s/map-of keyword? :quantit.indicator/indicator))

(defn- normalize-deps [dependency-mappings]
  (into {} (for [[k v] dependency-mappings]
             [k (csk/->kebab-case-keyword v)])))


(defn- declare-component
  ([deps]
   (fn [comp]
     [(csk/->kebab-case-keyword comp)
      `(component/using
         ((~constr-sym ~comp))
         ~deps)]))
  ([deps kw]
   (fn [comp]
     [kw
      `(component/using
         ((~constr-sym ~comp))
         ~deps)])))

(defmacro deftrader [name strategy indicators dependency-mappings]
  {:pre [(s/valid? symbol? name)
         ;(s/valid? :quantit.strategy/strategy strategy)
         ;(s/valid? :quantit.indicator/indicators indicators)
         ;(s/valid? ::dependency-mappings dependency-mappings)
         ]}
  (let [normalized-deps (normalize-deps dependency-mappings)]
    (prn normalized-deps)
    (let [
          system-components (->> indicators
                                 (map (declare-component normalized-deps))
                                 (concat [((declare-component normalized-deps :strategy) strategy)])
                                 (reduce into))]
      (prn system-components)
      `(def ~name
         (component/system-map
           ~@system-components)))))