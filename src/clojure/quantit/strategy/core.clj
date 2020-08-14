(ns quantit.strategy.core
  (:require [clojure.spec.alpha :as s]
            [quantit.indicator.core :refer [defindicator]]
            [quantit.component.core :refer [defcomponent]]
            [quantit.rule.core :as r]))


(defprotocol Strategy
  (entry? [this])
  (on-entry [this data])
  (exit? [this])
  (update? [this])                                          ;; TODO: Here update? and on-update should also accept the current position
  (on-update [this data]))

(defn strategy? [x] (and (class? x)
                         (extends? Strategy x)))

(def default-update? '(update? [_] (r/empty-rule)))

(defn update?-implemented? [body]
  (some #(= 'update? (first %)) body))

;; TODO: Augment body to add default implementation for update-state (or any other) methods if not provided
(defmacro defstrategy [name basis & body]
  (let [body (if (not (update?-implemented? body))
               (conj body default-update?)
               body)]
    `(defcomponent ~name ~basis Strategy ~@body)))