(ns quantit.strategy
  (:require [clojure.spec.alpha :as s]
            [quantit.indicator :refer [defindicator]]
            [quantit.component :refer [defcomponent]]))

(defprotocol Strategy
  (entry? [this bar history])
  (on-entry [this bar history])
  (exit? [this bar history])
  (on-exit [this bar history])
  (update? [this bar history])
  (on-update [this bar history]))

(defn strategy? [x] (instance? Strategy x))

(s/def ::strategy strategy?)

(defmacro defstrategy [name depsv & body]
  `(defcomponent ~name ~depsv Strategy ~@body))

(comment
  "Rough example"

  (defindicator MyLowerIndicator [] (value [this _ _] 10))
  (defindicator MyUpperIndicator [] (value [this _ _] 20))
  (defindicator MyIndicator [[:lower-indicator MyLowerIndicator]
                             [:upper-indicator MyUpperIndicator]]
    (value [this {:keys [lower-indicator upper-indicator] :as bar} _]
      (/ (+ upper-indicator lower-indicator) 2)))

  "Or maybe"

  (defindicator MyIndicator [] {:lower-indicator MyLowerIndicator
                                :upper-indicator MyUpperIndicator}
    (value [this {:keys [lower-indicator upper-indicator] :as bar} _]
      (/ (+ upper-indicator lower-indicator) 2))) -

  "Where the first argument after the Indicator name is a vector of symbols for state
  and they are initialized with (atom nil). If so, then the record methods should be memoized
  to avoid triggering side-effects multiple times. Then you could optimize records with no state arguments
  to have no memoization"

  "No need since you can just define global atoms, although it should be best if value
  methods were memoized"

  (defstrategy MyStrategy [[:my-indicator MyIndicator]]
    (entry? [this {:keys [open high low close volume my-indicator] :as input} _]
      (when (< 0 my-indicator)
        true))
    (on-entry [this {:keys [open high low close volume]} _]
      (buy 10))
    (exit? [this {:keys [open high low close volume my-indicator]}]
      (when (> 0 my-indicator)
        true))
    (on-exit [this {:keys [open high low close volume]} _]
      (sell 10))
    (update? [_ _ _] false)
    (on-update [_ _ _]))

  (backtest MyStrategy ...))
;; The comment above should compile to the comment below
(comment
  ;; First defindicator expression

  )