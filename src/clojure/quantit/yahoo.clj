(ns quantit.yahoo
  (:require [quantit.utils :refer :all])
  (:import (yahoofinance.histquotes Interval HistoricalQuote)
           (yahoofinance YahooFinance)
           (java.time LocalDate)))

(def kw->Interval {:daily   Interval/DAILY
                   :weekly  Interval/WEEKLY
                   :monthly Interval/MONTHLY})




(defn get-quotes [^String symbol ^LocalDate from ^LocalDate to interval]
  (let [interval (kw->Interval interval)
        from (zoned-date-time->calendar (nyse-market-open-date-time from))
        to (zoned-date-time->calendar (nyse-market-close-date-time to))]
    (mapv (fn [^HistoricalQuote quote]
            {:datetime  (-> quote .getDate .toInstant)
             :open      (-> quote .getOpen)
             :high      (-> quote .getHigh)
             :low       (-> quote .getLow)
             :close     (-> quote .getClose)
             :volume    (-> quote .getVolume)
             :adj-close (-> quote .getAdjClose)})
          (-> (YahooFinance/get symbol)
              (.getHistory from to interval)))))
