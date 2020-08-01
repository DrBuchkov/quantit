(ns quantit.execution
  (:require [quantit.adapter :as adp :refer [run-subscriber handle-order! update-before update-after]]
            [quantit.utils :refer [end? inspect]]
            [quantit.bar :as bar]
            [quantit.component :refer [update-state-before update-state-after]]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s])
  (:import (quantit.adapter SubscriberAdapter OrderAdapter)))


(defn update-system-state [trade-system bar bar-history update-statefn]
  ;{:pre [(s/valid? ::bar/bar bar)]}
  (loop [trade-system trade-system
         kvs (seq trade-system)]
    (if (empty? kvs)
      trade-system
      (let [[k v] (first kvs)
            new-state (update-statefn v bar bar-history)]
        (recur (assoc-in trade-system [k :state] new-state) (rest kvs))))))

(defn update-system-state-before [trade-system bar bar-history]
  (update-system-state trade-system bar bar-history update-state-before))

(defn update-system-state-after [trade-system bar bar-history]
  (update-system-state trade-system bar bar-history update-state-after))

;; TODO: Add some kind of capability to start with bar history older than system start or maybe even delegate the
;;        responsibility to the user to keep track of bar history through state
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
    (loop [trade-system trade-system
           bar (async/<!! barc)
           bar-history []]
      (if (not (end? bar))
        (let [trade-system (update-system-state-before trade-system bar bar-history)]
          (inspect bar)                                     ;; TODO: handle new incoming bar
          (recur (update-system-state-after trade-system bar bar-history)
                 (async/<!! barc)
                 (conj bar-history bar)))
        (do (async/>!! orderc bar))))))
