(ns quantit.sandbox
  (:require
    ;; Specs
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
    [quantit.indicator.core :refer [defindicator value]]
    [quantit.trade-system.core :refer [trade-system]]
    [quantit.utils :refer [zoned-date-time->calendar
                           nyse-market-open-date-time
                           nyse-market-close-date-time]]
    [quantit.backtest.core :refer [backtest]]
    [quantit.rule.core :as r]
    [quantit.order.core :refer [market-order-buy]]
    [clojure.core.match :as m]
    [tick.alpha.api :as t]))

(defindicator SimpleMovingAverage [:default-params {:window 10}]
  (value [this bars]
    (let [{:keys [window]} (:params this)]
      (when (>= (count bars) window)
        (/ (->> bars
                (take window)
                (map :adj-close)
                (reduce +))
           window)))))

(defstrategy SimpleMovingAverageStrategy [:dependencies [:short-sma :long-sma]]
  (entry? [this]
    (r/rule :sma-buy "Short SMA crosses over Long SMA"
            (fn [bars]
              (let [short-sma (-> this :short-sma (value bars))
                    long-sma (-> this :long-sma (value bars))]
                (and short-sma long-sma
                     (> short-sma long-sma))))))

  (on-entry [this {:keys [satisfied]}]
    (when (= satisfied :sma-buy)
      (market-order-buy 1)))

  (exit? [this]
    (r/rule :sma-sell "Long SMA crosses over Short SMA"
            (fn [bars]                                      ;; TODO: Need to be able to pass position
              (let [short-sma (-> this :short-sma (value bars))
                    long-sma (-> this :long-sma (value bars))]
                (< short-sma long-sma))))))

(def trader (trade-system :strategy SimpleMovingAverageStrategy
                          :indicators [[SimpleMovingAverage :-> :short-sma
                                        :params {:window 30}]
                                       [SimpleMovingAverage :-> :long-sma
                                        :params {:window 100}]]))

(comment (backtest trader "SPY" :daily (t/new-date 2010 12 19)))