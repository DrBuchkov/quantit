(ns quantit.yahoo
  (:require [quantit.utils :refer :all])
  (:import (yahoofinance.histquotes HistoricalQuote)
           (yahoofinance YahooFinance)
           (java.time LocalDate)))

(defn get-quotes [^String symbol ^LocalDate from ^LocalDate to interval]
  (let [interval (kw->Interval interval)
        from (zoned-date-time->calendar (nyse-market-open-date-time from))
        to (zoned-date-time->calendar (nyse-market-close-date-time to))]
    (mapv (fn [^HistoricalQuote quote]
            {:datetime  (-> quote .getDate .toInstant)
             :open      (-> quote .getOpen double)
             :high      (-> quote .getHigh double)
             :low       (-> quote .getLow double)
             :close     (-> quote .getClose double)
             :volume    (-> quote .getVolume double)
             :adj-close (-> quote .getAdjClose double)})
          (-> (YahooFinance/get symbol)
              (.getHistory from to interval)))))
