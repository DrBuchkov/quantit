(ns quantit.strategy.core
  (:require [clojure.spec.alpha :as s]
            [quantit.indicator.core :refer [defindicator]]
            [quantit.component.core :refer [defcomponent]]))


(defprotocol Strategy
  (entry? [this bar history])
  (on-entry [this bar history])
  (exit? [this bar history])
  (update? [this bar history])                              ;; TODO: Here update? and on-update should also accept the current position
  (on-update [this bar history]))

(defn strategy? [x] (and (class? x)
                         (extends? Strategy x)))

(def default-update? '(update? [_ _ _] false))

(defn update?-implemented? [body]
  (some #(= 'update? (first %)) body))

;; TODO: Augment body to add default implementation for update-state (or any other) methods if not provided
(defmacro defstrategy [name basis & body]
  (let [body (if (not (update?-implemented? body))
               (conj body default-update?)
               body)]
    `(defcomponent ~name ~basis Strategy ~@body)))