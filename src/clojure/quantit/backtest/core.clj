(ns quantit.backtest.core
  (:require [clojure.spec.alpha :as s]
            [quantit.execution.core :refer [run-trade-system]]
            [quantit.utils :refer [nyse-market-close-date-time nyse-market-open-date-time kw->Interval zoned-date-time->calendar end?]]
            [tick.alpha.api :as t])
  (:import (java.time LocalDate)
           (backtest BacktestSubscriberAdapter BacktestOrderAdapter)))


(defn- new-backtest-subscriber [^String symbol interval ^LocalDate from ^LocalDate to]
  (let [interval (kw->Interval interval)
        from (zoned-date-time->calendar (nyse-market-open-date-time from))
        to (zoned-date-time->calendar (nyse-market-close-date-time to))]
    (BacktestSubscriberAdapter. symbol from to interval)))

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
   (let [subscriber (new-backtest-subscriber symbol interval from to)
         orderer (BacktestOrderAdapter.)]
     (run-trade-system trade-system symbol subscriber orderer)
     (vec (.getOrders orderer)))))



