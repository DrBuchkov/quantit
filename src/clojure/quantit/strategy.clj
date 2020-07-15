(ns quantit.strategy
  (:require [clojure.spec.alpha :as s]
            [quantit.indicator :refer [defindicator]]
            [quantit.component :refer [defcomponent]]))


(defprotocol Strategy
  (entry? [this bar history state])
  (on-entry [this bar history state])
  (exit? [this bar history state])
  (on-exit [this bar history state])                        ;; TODO: Not clear if on-exit method is needed, since there's only one way you can close a position
  (update? [this bar history state])                        ;; TODO: Here update? and on-update should also accept the current position
  (on-update [this bar history state])
  (update-state-before [this bar history state])
  (update-state-after [this bar history state]))

(defn strategy? [x] (extends? Strategy x))

(s/def ::strategy strategy?)

;; TODO: Augment body to add default implementation for update-state (or any other) methods if not provided
(defmacro defstrategy [name depsv & body]
  `(defcomponent ~name ~depsv Strategy ~@body))