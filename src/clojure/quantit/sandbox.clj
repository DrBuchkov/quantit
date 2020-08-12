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
  (entry? [this]
    (r/or (r/rule :some-rule "MyLowerIndicator is higher than my MyUpperIndicator"
                  (fn [{:keys [bar bar-history]}]))
          (r/rule :other-rule "MyUpperIndicator is higher than my MyLowerIndicator")))
  (on-entry [this _ _]
    (buy 10))
  (exit? [this {:keys [my-indicator]} _]
    (when (> 0 my-indicator)
      true)))

(def trader (trade-system :strategy [MyStrategy :params {:my-param 1} :init-state {:some-state 0}]
                          :indicators [MyIndicator          ;; by default it's aliased as :my-indicator
                                       [MyLowerIndicator :-> :lower-indicator
                                        :params {:something 1}
                                        :init-state {:counter 0}]
                                       [MyUpperIndicator :-> :upper-indicator]]))

(comment (backtest trader "SPY" :daily (t/new-date 2019 12 19)))