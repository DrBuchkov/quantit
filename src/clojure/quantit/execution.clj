(ns quantit.execution
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component :refer [constr-sym depsfn-sym]]
            [quantit.indicator :as indicator]
            [quantit.strategy :as strategy]
            [quantit.utils :refer [flat-seq->map inspect get-component-deps]]))


(s/def ::extended-indicator-form-args (s/keys :opt-un [::init-state ::params]))
(s/def ::extended-indicator-form (s/cat :indicator-class ::indicator/indicator
                                        :alias-form (s/? (s/cat :->-kw #(= % :->)
                                                                :alias keyword?))
                                        :params-form (s/? (s/cat :params-kw #(= % :params)
                                                                 :params map?))
                                        :init-state-form (s/? (s/cat :init-state-kw #(= % :init-state)
                                                                     :init-state map?))))

(s/def ::indicator-form (s/or :basic ::indicator/indicator
                              :extended ::extended-indicator-form))

(s/def ::trader-declarations-map (s/keys :req-un [::strategy ::indicators]))

(s/def ::trader-declarations #(->> % (flat-seq->map) (s/valid? ::trader-declarations-map)))

(defn- normalize-deps [dependency-mappings]
  (into {} (for [[k v] dependency-mappings]
             [k (csk/->kebab-case-keyword v)])))

;; TODO: should filter deps to only contain the dependencies of each component
;;        to avoid circular dependencies
(defn- declare-component
  ([deps-map]
   (fn [comp]
     (let [deps ((eval (depsfn-sym comp)))
           deps-map (->> deps-map
                         (filterv (fn [[k _]] (some #(= k %) deps)))
                         (into {}))]
       [(csk/->kebab-case-keyword comp)
        `(component/using
           (~(constr-sym comp))
           ~deps-map)])))
  ([deps-map kw]
   (fn [comp]
     (let [deps ((eval (depsfn-sym comp)))
           deps-map (->> deps-map
                         (filterv (fn [[k _]] (some #(= k %) deps)))
                         (into {}))]
       [kw
        `(component/using
           (~(constr-sym comp))
           ~deps-map)])
     )))

(defn indicator-forms->map [ind-form]
  (cond
    (vector? ind-form) (let [{:keys [-> params init-state]}
                             (->> ind-form
                                  (rest)
                                  (partition 2)
                                  (map vec)
                                  (into {}))]
                         [-> {:params     params
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
  (let [{:keys [strategy indicators]} (flat-seq->map body)
        indicator-mappings (into {} (mapv indicator-forms->map indicators))
        indicator-symbols (mapv (fn [[_ v]] (indicator-mapping->indicator v))
                                indicator-mappings)
        dependency-mappings (into {} (mapv indicator-mapping->dependency-mapping
                                           indicator-mappings))
        system-components (->> indicator-symbols
                               (mapv (declare-component dependency-mappings))
                               (concat [((declare-component dependency-mappings :strategy) strategy)])
                               (reduce into))]
    `(def ~name
       (component/system-map
         ~@system-components))))