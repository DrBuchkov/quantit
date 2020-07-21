(ns quantit.execution
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component :refer [constr-sym]]
            [quantit.indicator :as indicator]
            [quantit.strategy :as strategy]
            [quantit.utils :refer [flat-seq->map inspect]]))


(s/def ::extended-indicator-form-args (s/keys :opt-un [::init-state ::params]))
(s/def ::extended-indicator-form (s/cat :indicator-class ::indicator/indicator
                                        :as-kw #(= % :as)
                                        :alias keyword?
                                        :args (s/? ::extended-indicator-form-args)))

(s/def ::indicator-form (s/or :basic ::indicator/indicator
                              :extended ::extended-indicator-form))

(s/def ::trader-declarations-map (s/keys :req-un [::strategy ::indicators]))

(s/def ::trader-declarations #(->> % (flat-seq->map) (s/valid? ::trader-declarations-map)))

(defn- normalize-deps [dependency-mappings]
  (into {} (for [[k v] dependency-mappings]
             [k (csk/->kebab-case-keyword v)])))


(defn- declare-component
  ([deps]
   (fn [comp]
     [(csk/->kebab-case-keyword comp)
      `(component/using
         (~(constr-sym comp))
         ~deps)]))
  ([deps kw]
   (fn [comp]
     [kw
      `(component/using
         (~(constr-sym comp))
         ~deps)])))

(defn indicator-forms->map [ind-form]
  (cond
    (vector? ind-form) (let [{:keys [as params init-state]}
                             (if (map? (last ind-form))
                               (merge {:as (ind-form 2)} (last ind-form))
                               {:as (ind-form 2)})]
                         [as {:params     params
                              :init-state init-state
                              :indicator  (first ind-form)}])
    :else [(csk/->kebab-case-keyword ind-form) ind-form]))

(defn indicator-mapping->indicator [mapping]
  (or (:indicator mapping)
      mapping))

(defn indicator-mapping->dependency-mapping [[k v]]
  [k (-> v indicator-mapping->indicator csk/->kebab-case-keyword)])

(defmacro deftrader [name & body]
  {:pre [(s/valid? symbol? name)
         (s/valid? ::trader-declarations body)]}
  (let [{:keys [strategy indicators] :as body-map} (flat-seq->map body)
        indicator-mappings (into {} (mapv indicator-forms->map indicators))
        indicator-symbols (mapv (fn [[_ v]] (indicator-mapping->indicator v))
                                indicator-mappings)
        dependency-mappings (into {} (mapv indicator-mapping->dependency-mapping
                                           indicator-mappings))
        system-components (->> indicator-symbols
                               (mapv (declare-component dependency-mappings))
                               (reduce into)
                               (conj [((declare-component dependency-mappings :strategy) strategy)])
                               (reduce into))]
    `(def ~name
       (component/system-map
         ~@system-components))))