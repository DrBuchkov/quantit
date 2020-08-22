(ns quantit.backtest.core
  (:require [clojure.spec.alpha :as s]
            [quantit.execution.core :refer [run-trade-system]]
            [quantit.utils :refer [nyse-market-close-date-time nyse-market-open-date-time kw->Interval zoned-date-time->calendar end?]]
            [quantit.yahoo :as yahoo]
            [quantit.dataset.core :as d]
            [tick.alpha.api :as t]
            [clojure.core.matrix.dataset :as ds])
  (:import (java.time LocalDate)
           (backtest BacktestSubscriberAdapter BacktestOrderAdapter)))


(defn- init-backtest-adapters [^String symbol interval ^LocalDate from ^LocalDate to]
  (let [quotes (yahoo/get-quotes symbol from to interval)]
    [(BacktestSubscriberAdapter. quotes) (BacktestOrderAdapter.)]))

(defn backtest
  ;([trade-system symbol bar-series]
  ; {:pre [(s/valid? :quantit.bar/bar-series bar-series)]}
  ; (let [sub-adapter (BacktestSubscriberAdapter.)
  ;       order-adapter ]
  ;   (run-trade-system symbol trade-system sub-adapter adapter)
  ;   (:orders adapter)))

  ([trade-system ^String symbol interval ^LocalDate from]
   (backtest trade-system symbol interval from (t/new-date)))

  ([trade-system ^String symbol interval ^LocalDate from ^LocalDate to]
   {:pre [(s/valid? :quantit.backtest/interval interval)]}
   (let [[subscriber orderer] (init-backtest-adapters symbol interval from to)]
     (run-trade-system trade-system symbol subscriber orderer)
     (let [orders-ds (d/mapseq->ds (vec (.getOrders orderer)))
           bars-ds (d/mapseq->ds (vec (.getBars subscriber)))]
       (ds/merge-datasets bars-ds orders-ds)))))