(ns quantit.execution
  (:require [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [com.stuartsierra.component :as component]
            [clojure.core.match :refer [match]]
            [quantit.component :refer [constr-sym deps-kw]]
            [quantit.indicator :as indicator]
            [quantit.utils :refer [flat-seq->map inspect get-component-deps]]))

(s/def ::params map?)
(s/def ::init-state map?)
(s/def ::extended-indicator-form-args (s/keys :opt-un [::init-state ::params]))
(s/def ::extended-indicator-form (s/cat :indicator-class ::indicator/indicator
                                        :alias-form (s/? (s/cat :->-kw #(= % :->)
                                                                :alias keyword?))
                                        :params-form (s/? (s/cat :params-kw #(= % :params)
                                                                 :params ::params))
                                        :init-state-form (s/? (s/cat :init-state-kw #(= % :init-state)
                                                                     :init-state ::init-state))))

(s/def ::indicator-form (s/or :basic ::indicator/indicator
                              :extended ::extended-indicator-form))

(s/def ::trade-system-declarations-map (s/keys :req-un [::strategy ::indicators]
                                               :opt-un [::params ::init-state]))

(s/def ::trade-system-declarations #(->> % (flat-seq->map) (s/valid? ::trade-system-declarations-map)))

(defn- declare-component [indicator-map comp]
  (let [component ((eval (constr-sym comp)))
        deps (deps-kw component)
        params (:params (indicator-map comp))
        state (:init-state (indicator-map comp))
        deps-map (->> (inspect indicator-map)
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

(defmacro trade-system [& body]
  {:pre [(s/valid? ::trade-system-declarations body)]}
  (let [{:keys [strategy indicators params init-state]} (flat-seq->map body)
        indicator-mappings (into {} (mapv indicator-forms->map indicators))
        indicator-symbols (indicator-mapping->symbols indicator-mappings)
        system-components (->> indicator-symbols
                               (mapv (fn [component] [(csk/->kebab-case-keyword component) (declare-component indicator-mappings component)]))
                               (concat [[:strategy (declare-component (-> indicator-mappings
                                                                          (assoc strategy {:params     params
                                                                                           :init-state init-state}))
                                                                      strategy)]])
                               (reduce into))
        ]
    `(component/system-map
       ~@system-components)))