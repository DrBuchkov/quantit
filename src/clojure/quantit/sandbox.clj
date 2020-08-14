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
    [quantit.rule.spec :refer :all]
    ;; Deps
    [quantit.strategy.core :refer [defstrategy]]
    [quantit.indicator.core :refer [defindicator]]
    [quantit.trade-system.core :refer [trade-system]]
    [quantit.utils :refer [zoned-date-time->calendar
                           nyse-market-open-date-time
                           nyse-market-close-date-time]]
    [quantit.backtest.core :refer [backtest]]
    [quantit.rule.core :as r]
    [clojure.core.match :as m]
    [tick.alpha.api :as t]))

(defindicator SimpleMovingAverage [:default-params {:window 10}]
  (value [this bars]
    (let [{:keys [window]} (:params this)]
      (when (>= (count bars) window)
        (->> bars
             (take window)
             (reduce +)
             (/ window))))))

(defstrategy SimpleMovingAverageStrategy [:dependencies [:short-sma :long-sma]]
  (entry? [this]
    (r/rule :sma-buy "Short SMA crosses over Long SMA"
            (fn [{:keys [short-sma long-sma]}]
              (when (and short-sma
                         long-sma
                         (> short-sma long-sma))
                true))))

  (on-entry [this {:keys [satisfied]}]
    (when (= satisfied :sma-buy)
      {:buy 1}))

  (exit? [this]
    (r/rule :sma-sell "Long SMA crosses over Short SMA"
            (fn [{:keys [short-sma long-sma]}]
              (when (< short-sma long-sma)
                true)))))

(def trader (trade-system :strategy SimpleMovingAverageStrategy
                          :indicators [[SimpleMovingAverage :-> :short-sma
                                        :params {:window 30}]
                                       [SimpleMovingAverage :-> :long-sma
                                        :params {:window 100}]]))

(comment (backtest trader "SPY" :daily (t/new-date 2019 12 19)))