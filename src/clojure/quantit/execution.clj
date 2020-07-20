(ns quantit.execution
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component :refer [constr-sym]]
            [quantit.indicator :as indicator]
            [quantit.utils :refer [extended-indicator-form-args->map]]))


(s/def ::dependency-mappings (s/map-of keyword? :quantit.indicator/indicator))

(s/def ::extended-indicator-form-args (s/keys :req-un [::as]
                                              :opt-un [::params ::init-state]))

(s/def ::extended-indicator-form (s/and #(->> % first (s/valid? ::indicator/indicator))
                                        #(->> %
                                              (extended-indicator-form-args->map)
                                              (s/valid? ::extended-indicator-form-args))))

(s/def ::indicator-form (s/or :basic ::indicator/indicator
                              :extended ::extended-indicator-form))

(s/def ::indicator-forms (s/coll-of ::indicator-form))

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

(defn with-indicators [& body]
  {:pre [(s/valid? ::indicator-forms body)]}
  (prn body)
  (->> body
       (map (fn [ind-form]
              (cond
                (vector? ind-form) (let [{:keys [as params init-state]} (extended-indicator-form-args->map ind-form)]
                                     [as (first ind-form)])
                :else [(csk/->kebab-case-keyword ind-form) ind-form])))
       (into {}))
  )

(defmacro deftrader [name strategy dependency-mappings]
  {:pre [(s/valid? symbol? name)
         ;(s/valid? :quantit.strategy/strategy strategy)
         ;(s/valid? :quantit.indicator/indicators indicators)
         ;(s/valid? ::dependency-mappings dependency-mappings)
         ]}
  (let [indicators (for [[_ v] dependency-mappings] v)
        normalized-deps (normalize-deps dependency-mappings)]
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