(ns quantit.sandbox
  (:require [quantit.strategy :refer [defstrategy]]
            [quantit.indicator :refer [defindicator]]
            [quantit.trade-system :refer [trade-system]]
            [quantit.utils :refer [zoned-date-time->calendar
                                   nyse-market-open-date-time
                                   nyse-market-close-date-time]]
            [quantit.backtest :refer [backtest]]
            [com.stuartsierra.component :as component]
            [tick.alpha.api :as t]))

(comment
  (defrecord MyLowerIndicator []
    Indicator
    (value [_ _ _] (rand-int 100)))

  (defrecord MyUpperIndicator []
    Indicator
    (value [_ _ _] (rand-int 100)))

  (defrecord MyIndicator [lower-indicator upper-indicator]
    Indicator
    (value [this {:keys [lower-indicator upper-indicator]} _]
      (- upper-indicator lower-indicator)))

  (defrecord MyStrategy [my-indicator]
    Strategy
    (entry? [this {:keys [open high low close volume my-indicator] :as input} _]
      (when (< 0 my-indicator)
        true))
    (on-entry [this _ _])
    (exit? [this {:keys [open high low close volume my-indicator]} _]
      (when (> 0 my-indicator)
        true))
    (on-exit [this _ _])
    (update? [_ _ _] false)
    (on-update [_ _ _]))

  (def execution
    (component/system-map
      :my-lower-indicator (->MyLowerIndicator)
      :my-upper-indicator (->MyUpperIndicator)
      :my-indicator (component/using
                      (map->MyIndicator {})
                      {:lower-indicator :my-lower-indicator
                       :upper-indicator :my-upper-indicator})
      :my-strategy (component/using
                     (map->MyStrategy {})
                     {:my-indicator :my-indicator}))))

(defindicator MyLowerIndicator []
  (value [this _ _] 10)
  (update-state-before [this _ _] (:state this))
  (update-state-after [this _ _] (:state this)))

(defindicator MyUpperIndicator []
  (value [this _ _] 20)
  (update-state-before [this _ _] (:state this))
  (update-state-after [this _ _] (:state this)))

(defindicator MyIndicator [:dependencies [:lower-indicator :upper-indicator]]
  (value [this {:keys [upper-indicator lower-indicator]} _]
    (/ (+ upper-indicator
          lower-indicator)
       2))
  (update-state-before [this _ _] (:state this))
  (update-state-after [this _ _] (:state this)))

(defstrategy MyStrategy [:dependencies [:my-indicator]]
  (entry? [this {:keys [my-indicator] :as input} _]
    (when (< 0 my-indicator)
      true))
  (on-entry [this _ _])
  (exit? [this {:keys [my-indicator]} _]
    (when (> 0 my-indicator)
      true))
  (on-exit [this _ _])
  (update? [_ _ _] false)
  (on-update [_ _ _])
  (update-state-before [this _ _] (:state this))
  (update-state-after [this _ _] (:state this)))

(def trader (trade-system :strategy MyStrategy
                          :params {:my-param 1}
                          :init-state {:some-state 0}
                          :indicators [MyIndicator          ;; by default it's aliased as :my-indicator
                                       [MyLowerIndicator :-> :lower-indicator
                                        :params {:something 1}
                                        :init-state {:my-state 0}]
                                       [MyUpperIndicator :-> :upper-indicator]]))

(comment (backtest trader "SPY" :daily (t/new-date 2019 12 19)))