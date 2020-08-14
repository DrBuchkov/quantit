(ns quantit.rule.core
  (:refer-clojure :exclude [or and])
  (:require [clojure.spec.alpha :as s]))

(defprotocol Rule
  (satisfied? [this bars]))

(defrecord BaseRule [name description satf]
  Rule
  (satisfied? [_ bars] (when (satf bars)
                         name)))

(defn rule [name description satf]
  {:pre [(s/valid? :quantit.rule/name name)
         (s/valid? :quantit.rule/description description)
         (s/valid? :quantit.rule/satisfied-fn satf)]}
  (->BaseRule name description satf))

(defrecord OrRule [subrules]
  Rule
  (satisfied? [_ bars] (->> subrules
                            (mapv #(satisfied? % bars))
                            (some identity))))

(defn or [& subrules]
  (->OrRule subrules))

(defrecord AndRule [subrules]
  Rule
  (satisfied? [_ bars]
    (let [satisfied (mapv #(satisfied? % bars) subrules)]
      (when (every? identity satisfied)
        satisfied))))

(defn and [& subrules]
  (->AndRule subrules))

(defn empty-rule []
  (rule :empty-rule "Empty rule, always false"
        (fn [_] false)))