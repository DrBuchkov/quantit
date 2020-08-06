(ns quantit.trade-system.core
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component.core :refer [constr-sym deps-kw]]
            [quantit.utils :refer [flat-seq->map inspect get-component-deps]]))

(defn- declare-component [comp {:keys [params state]} aliases]
  (let [component ((eval (constr-sym comp)))
        deps (deps-kw component)
        deps-map (->> aliases
                      (filter (fn [[k _]] (some #(= k %) deps)))
                      (into {}))]
    `(component/using
       ~(let [component (if (some? params) (assoc component :params params) component)
              component (if (some? state) (assoc component :state state) component)]
          component)
       ~deps-map)))



(defn- indicator-forms->map [ind-form]
  (cond
    (vector? ind-form) (let [{:keys [-> params init-state]} (->> ind-form rest flat-seq->map)]
                         [(first ind-form) {:params     params
                                            :init-state init-state
                                            :alias      ->}])
    :else [ind-form {:alias (csk/->kebab-case-keyword ind-form)}]))

(defn- indicator-mapping->symbols [mappings]
  (->> mappings
       (mapv (fn [[k _]] k))))

(defn- conformed-indicator->alias-map [ind]
  (condp = (first ind)
    :basic (let [k (csk/->kebab-case-keyword (last ind))]
             [k k])
    :extended (let [k (-> (last ind)
                          (get-in [:alias-form :alias]))

                    v (-> (last ind)
                          (get-in [:indicator-class])
                          csk/->kebab-case-keyword)]
                [k v])))

(defn conformed-indicator->opts [indform]
  (condp = (first indform)
    :basic {(last indform) {}}
    :extended (let [val (last indform)]
                {(:indicator-class val)
                 {:params (get-in val [:params-form :params])
                  :state  (get-in val [:init-state-form :init-state])}})))

(defn conformed-indicator->sym [indform]
  (condp = (first indform)
    :basic (last indform)
    :extended (:indicator-class (last indform))))


(defmacro trade-system [& {:keys [strategy indicators]}]
  {:pre [(s/valid? :quantit.trade-system/strategy-form strategy)
         (s/valid? :quantit.trade-system/indicator-forms indicators)]}
  (let [conformed-indicators (s/conform :quantit.trade-system/indicator-forms indicators)
        aliases (->> conformed-indicators
                     (mapv conformed-indicator->alias-map)
                     (into {}))
        indicator-opts (->> conformed-indicators
                            (mapv conformed-indicator->opts)
                            (into {}))
        indicator-symbols (->> conformed-indicators (mapv conformed-indicator->sym))
        conformed-strategy-map (->> strategy
                                    (s/conform :quantit.trade-system/strategy-form)
                                    flat-seq->map)
        strategy-sym (or (:basic conformed-strategy-map)
                         (get-in conformed-strategy-map [:extended :strategy-class]))
        strategy-params (get-in conformed-strategy-map
                                [:extended :params-form :params])
        strategy-init-state (get-in conformed-strategy-map
                                    [:extended :init-state-form :init-state])
        system-components (->> indicator-symbols
                               (mapv (fn [component] [(csk/->kebab-case-keyword component)
                                                      (declare-component component (indicator-opts
                                                                                     component)
                                                                         aliases)]))
                               (concat [[:strategy (declare-component
                                                     strategy-sym
                                                     {:params strategy-params
                                                      :state  strategy-init-state}
                                                     aliases)]])
                               (reduce into))]
    `(component/start-system
       (component/system-map
         ~@system-components)))
  )