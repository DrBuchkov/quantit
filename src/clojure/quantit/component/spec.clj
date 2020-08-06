(ns quantit.component.spec
  (:require [clojure.spec.alpha :as s]
            [quantit.component.core :refer [map-constr-sym constr-sym]]))


(s/def :quantit.component/dependencies (s/coll-of keyword? :kind vector?))
(s/def :quantit.component/default-params map?)
(s/def :quantit.component/default-init-state map?)

(s/def :quantit.component/basis (s/cat :dependencies (s/? (s/cat :k #(= % :dependencies)
                                                                 :v :quantit.component/dependencies))
                                       :default-params (s/? (s/cat :k #(= % :default-params)
                                                                   :v :quantit.component/default-params))
                                       :default-init-state (s/? (s/cat :k #(= % :default-init-state)
                                                                       :v :quantit.component/default-init-state))))

(s/def :quantit.component/type #{'quantit.indicator.core/Indicator 'quantit.strategy.core/Strategy})


(s/fdef map-constr-sym
        :args (s/cat :name symbol?)
        :ret symbol?)

(s/fdef constr-sym
        :args (s/cat :name symbol?)
        :ret symbol?)