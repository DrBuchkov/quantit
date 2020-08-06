(ns quantit.trade-system.core
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component.core :refer [constr-sym deps-kw]]
            [quantit.utils :refer [flat-seq->map inspect get-component-deps]]))

(defn- declare-component [indicator-map comp]
  (let [component ((eval (constr-sym comp)))
        deps (deps-kw component)
        params (:params (indicator-map comp))
        state (:init-state (indicator-map comp))
        deps-map (->> indicator-map
                      (filterv (fn [[_ v]] (some #(= (:alias v) %) deps)))
                      (mapv (fn [[k v]] [(:alias v) (csk/->kebab-case-keyword k)]))
                      (into {}))]
    `(component/using
       ~(let [component (if (some? params) (assoc component :params params) component)
              component (if (some? state) (assoc component :state state) component)]
          component)
       ~deps-map)))



(defn- indicator-forms->map [ind-form]
  (cond
    (vector? ind-form) (let [{:keys [-> params init-state]} (->> ind-form
                                                                 (rest)
                                                                 (flat-seq->map))]
                         [(first ind-form) {:params     params
                                            :init-state init-state
                                            :alias      ->}])
    :else [ind-form {:alias (csk/->kebab-case-keyword ind-form)}]))

(defn- indicator-mapping->symbols [mappings]
  (->> mappings
       (mapv (fn [[k _]] k))))

(defmacro trade-system [& {:keys [strategy params init-state indicators]}]
  {:pre [(s/valid? symbol? strategy)
         (s/valid? :quantit.trade-system/indicator-forms indicators)]}
  (let [indicator-mappings (into {} (mapv indicator-forms->map indicators))
        indicator-symbols (indicator-mapping->symbols indicator-mappings)
        system-components (->> indicator-symbols
                               (mapv (fn [component] [(csk/->kebab-case-keyword component)
                                                      (declare-component indicator-mappings component)]))
                               (concat [[:strategy (declare-component
                                                     (-> indicator-mappings
                                                         (assoc strategy {:params     params
                                                                          :init-state init-state}))
                                                     strategy)]])
                               (reduce into))]
    `(component/start-system
       (component/system-map
         ~@system-components))))