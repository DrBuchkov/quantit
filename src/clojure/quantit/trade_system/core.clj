(ns quantit.trade-system.core
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component.core :refer [constr-sym deps-kw]]
            [quantit.utils :refer [flat-seq->map]]))

(defn- declare-component [comp {:keys [params state]}]
  (let [component ((eval (constr-sym comp)))
        deps (deps-kw component)]
    `(component/using
       ~(let [component (if (some? params) (assoc component :params params) component)
              component (if (some? state) (assoc component :state state) component)]
          component)
       ~deps)))

(defn- ind->alias [ind]
  (condp = (first ind)
    :basic (csk/->kebab-case-keyword (last ind))
    :extended (-> (last ind)
                  (get-in [:alias-form :alias]))))

(defn ind->opts [indform]
  (condp = (first indform)
    :basic {}
    :extended (let [val (last indform)]
                {:params (get-in val [:params-form :params])
                 :state  (get-in val [:init-state-form :init-state])})))

(defn ind->sym [indform]
  (condp = (first indform)
    :basic (last indform)
    :extended (:indicator-class (last indform))))


(defmacro trade-system [& {:keys [strategy indicators]}]
  {:pre [(s/valid? :quantit.trade-system/strategy-form strategy)
         (s/valid? :quantit.trade-system/indicator-forms indicators)]}
  (let [conformed-indicators (s/conform :quantit.trade-system/indicator-forms indicators)
        aliases (->> conformed-indicators
                     (mapv ind->alias))
        indicator-opts (->> conformed-indicators
                            (mapv ind->opts))
        alias-opt-map (->> indicator-opts
                           (mapv (fn [alias opts] [alias opts]) aliases)
                           (into {}))
        indicator-symbols (->> conformed-indicators (mapv ind->sym))
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
                               (mapv (fn [alias indicator]
                                       [alias
                                        (declare-component indicator (alias-opt-map
                                                                       alias))]) aliases)
                               (concat [[:strategy (declare-component
                                                     strategy-sym
                                                     {:params strategy-params
                                                      :state  strategy-init-state})]])
                               (reduce into))]
    `(component/start-system
       (component/system-map
         ~@system-components))))