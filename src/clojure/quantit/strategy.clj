(ns quantit.strategy
  (:require [clojure.spec.alpha :as s]
            [quantit.indicator :refer [defindicator]]
            [quantit.component :refer [defcomponent]]))


(defprotocol Strategy
  (entry? [this bar history])
  (on-entry [this bar history])
  (exit? [this bar history])
  (on-exit [this bar history])                              ;; TODO: Not clear if on-exit method is needed, since there's only one way you can close a position
  (update? [this bar history])                              ;; TODO: Here update? and on-update should also accept the current position
  (on-update [this bar history])
  (update-state-before [this bar history])                  ;; Updates state before handling bar
  (update-state-after [this bar history]))                  ;; Update state after handling bar

(defn strategy? [x] (and (class? x)
                         (extends? Strategy x)))

(s/def ::strategy strategy?)

;; TODO: Augment body to add default implementation for update-state (or any other) methods if not provided
(defmacro defstrategy [name basis & body]
  `(defcomponent ~name ~basis Strategy ~@body))