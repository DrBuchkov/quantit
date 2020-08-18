(ns quantit.backtest.core
  (:require [clojure.spec.alpha :as s]
            [clojure.core.async :as async]
            [quantit.adapter.core :refer [SubscriberAdapter OrderAdapter]]
            [quantit.execution.core :refer [run-trade-system]]
            [quantit.yahoo :as yahoo]
            [quantit.utils :refer [nyse-market-close-date-time end?]]
            [tick.alpha.api :as t])
  (:import (java.time LocalDate)))

(defrecord BacktestAdapter [bar-series orders]
  SubscriberAdapter
  (run-subscriber [this datac]
    (doseq [bar (conj bar-series 'end)]
      (async/>!! datac bar)))

  OrderAdapter
  (handle-order! [this order] order)
  (update-before [this order] this)
  (update-after [this order] (update this :orders conj order)))

(defn new-BacktestAdapter [bar-series]
  (->BacktestAdapter bar-series []))


(defn backtest
  ([trade-system symbol bar-series]
   {:pre [(s/valid? :quantit.bar/bar-series bar-series)]}
   (let [backtest-adapter (new-BacktestAdapter bar-series)]
     (run-trade-system symbol trade-system backtest-adapter backtest-adapter)
     (:orders backtest-adapter)))

  ([trade-system ^String symbol interval ^LocalDate from]
   (backtest trade-system symbol interval from (t/new-date)))

  ([trade-system ^String symbol interval ^LocalDate from ^LocalDate to]
   {:pre [(s/valid? :quantit.backtest/interval interval)]}
   (let [bar-series (yahoo/get-quotes symbol from to interval)
         backtest-adapter (new-BacktestAdapter bar-series)]
     (run-trade-system trade-system symbol backtest-adapter backtest-adapter)
     (:orders backtest-adapter))))
