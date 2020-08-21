(ns quantit.utils
  (:require [tick.alpha.api :as t])
  (:import (java.time ZonedDateTime)
           (java.util GregorianCalendar)
           (yahoofinance.histquotes Interval)))

(defn end? [x] (= x :end))

(defn vec-or-seq? [x]
  (or (vector? x)
      (seq? x)))

(defn flat-seq->map [x]
  (->> x (partition 2) (map vec) (into {})))

(defn inspect [body]
  (prn body)
  body)

(def kw->Interval {:daily   Interval/DAILY
                   :weekly  Interval/WEEKLY
                   :monthly Interval/MONTHLY})

(defn nyse-market-open-date-time
  ([]
   (-> (t/new-date)
       (t/at "09:30")
       (t/in "GMT-6")))
  ([local-date]
   (-> local-date
       (t/at "09:30")
       (t/in "GMT-6"))))

(defn nyse-market-close-date-time
  ([]
   (-> (t/new-date)
       (t/at "16:00")
       (t/in "GMT-6")))
  ([local-date]
   (-> local-date
       (t/at "16:00")
       (t/in "GMT-6"))))

(defn zoned-date-time->calendar [^ZonedDateTime date-time]
  (GregorianCalendar/from date-time))
