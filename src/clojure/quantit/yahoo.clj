(ns quantit.yahoo
  (:require [quantit.utils :refer :all])
  (:import (yahoofinance YahooFinance)
           (java.time LocalDate)))

(defn get-quotes [^String symbol ^LocalDate from ^LocalDate to interval]
  (let [interval (kw->Interval interval)
        from (zoned-date-time->calendar (nyse-market-open-date-time from))
        to (zoned-date-time->calendar (nyse-market-close-date-time to))]
    (-> (YahooFinance/get symbol)
        (.getHistory from to interval))))
