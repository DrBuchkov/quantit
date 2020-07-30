(ns quantit.execution
  (:require [quantit.adapter :as adp :refer [run-subscriber run-orders]]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s])
  (:import (quantit.adapter SubscriberAdapter OrderAdapter)))

(defn end? [x] (= x 'end))

(defn run-trade-system [trade-system ^SubscriberAdapter subscriber ^OrderAdapter orderer]
  {:pre [(s/valid? ::adp/subscriber subscriber)
         (s/valid? ::adp/orderer orderer)]}
  (let [datac (async/chan)
        orderc (async/chan)]
    (async/go (run-subscriber subscriber datac))
    (async/go (run-orders orderer orderc))
    (loop [input (async/<!! datac)]
      (when (not (end? input))
        (do (prn input)
            (recur (async/<!! datac)))))))                  ;; TODO: handle new incoming bar
