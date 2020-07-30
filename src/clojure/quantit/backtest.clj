(ns quantit.backtest
  (:require [clojure.spec.alpha :as s]
            [clojure.core.async :as async]
            [quantit.adapter :refer [SubscriberAdapter OrderAdapter]]
            [quantit.execution :refer [run-trade-system]]
            [quantit.yahoo :as yahoo]
            [quantit.utils :refer [nyse-market-close-date-time]]
            [quantit.bar :as bar]
            [tick.alpha.api :as t])
  (:import (java.time LocalDate Year)))

(def intervals #{:daily :weekly :monthly})

(s/def ::interval intervals)


(defrecord BacktestAdapter [bar-series]
  SubscriberAdapter
  (run-subscriber [this datac]
    (doseq [bar (conj bar-series 'end)]
      (async/>!! datac bar)))

  OrderAdapter
  (run-orders [this orderc] nil))


(defn backtest
  ([trade-system bar-series]
   {:pre [(s/valid? ::bar/bar-series bar-series)]}
   (let [backtest-adapter (->BacktestAdapter bar-series)]
     (run-trade-system trade-system backtest-adapter backtest-adapter)))

  ([trade-system ^String symbol interval ^LocalDate from]
   (backtest trade-system symbol interval from (t/new-date)))

  ([trade-system ^String symbol interval ^LocalDate from ^LocalDate to]
   {:pre [(s/valid? ::interval interval)]}
   (let [bar-series (yahoo/get-quotes symbol from to interval)
         backtest-adapter (->BacktestAdapter bar-series)]
     (run-trade-system trade-system backtest-adapter backtest-adapter))))
