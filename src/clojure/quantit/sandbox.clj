(ns quantit.sandbox
  (:require
    ;; Specs
    [quantit.adapter.spec :refer :all]
    [quantit.backtest.spec :refer :all]
    [quantit.bar.spec :refer :all]
    [quantit.component.spec :refer :all]
    [quantit.indicator.spec :refer :all]
    [quantit.order.spec :refer :all]
    [quantit.strategy.spec :refer :all]
    [quantit.trade-system.spec :refer :all]
    ;; Deps
    [quantit.strategy.core :refer [defstrategy]]
    [quantit.indicator.core :refer [defindicator]]
    [quantit.trade-system.core :refer [trade-system]]
    [quantit.utils :refer [zoned-date-time->calendar
                           nyse-market-open-date-time
                           nyse-market-close-date-time]]
    [quantit.backtest.core :refer [backtest]]
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
  (update-state-before [this _ _]
    (let [{:keys [state]} this]
      (update state :counter inc))))

(defindicator MyUpperIndicator []
  (value [this _ _] 20))

(defindicator MyIndicator [:dependencies [:lower-indicator :upper-indicator]]
  (value [this {:keys [upper-indicator lower-indicator]} _]
    (/ (+ upper-indicator
          lower-indicator)
       2)))

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
  (on-update [_ _ _]))

(def trader (trade-system :strategy [MyStrategy :params {:my-param 1} :init-state {:some-state 0}]
                          :indicators [MyIndicator          ;; by default it's aliased as :my-indicator
                                       [MyLowerIndicator :-> :lower-indicator
                                        :params {:something 1}
                                        :init-state {:counter 0}]
                                       [MyUpperIndicator :-> :upper-indicator]]))

(comment
  "Non-macro version of trade-system"
  (def trader (trade-system :strategy (map->MyStrategy {:params {:my-param 1}
                                                        :state  {:some-state 0}})
                            :indicators [(map->MyIndicator {}) ;; by default it's aliased as :my-indicator
                                         [(map->MyIndicator {:params     {:something 1}
                                                             :init-state {:counter 0}})
                                          :-> :lower-indicator]
                                         [(map->MyUpperIndicator {}) :-> :upper-indicator]])))

(comment (backtest trader "SPY" :daily (t/new-date 2019 12 19)))