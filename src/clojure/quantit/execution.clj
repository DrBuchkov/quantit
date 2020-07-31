(ns quantit.execution
  (:require [quantit.adapter :as adp :refer [run-subscriber handle-order! update-before update-after]]
            [quantit.utils :refer [end?]]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s])
  (:import (quantit.adapter SubscriberAdapter OrderAdapter)))

(defn run-trade-system [trade-system ^SubscriberAdapter subscriber ^OrderAdapter orderer]
  {:pre [(s/valid? ::adp/subscriber subscriber)
         (s/valid? ::adp/orderer orderer)]}
  (let [barc (async/chan)
        orderc (async/chan)]
    (async/go (run-subscriber subscriber barc))
    (async/go (loop [orderer orderer
                     order (async/<! orderc)]
                (when (not (end? order))
                  (let [orderer (update-before orderer order)]
                    (handle-order! orderer order)
                    (recur (update-after orderer order) (async/<! orderc))))))
    (loop [bar (async/<!! barc)]
      (if (not (end? bar))
        (do (prn bar)                                       ;; TODO: handle new incoming bar
            (recur (async/<!! barc)))
        (do (async/>!! orderc bar))))))
