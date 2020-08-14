(ns quantit.execution.core
  (:require [quantit.adapter.core :refer [run-subscriber handle-order! update-before update-after]]
            [quantit.component.core :refer [update-state-before update-state-after]]
            [quantit.utils :refer [end? inspect]]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component])
  (:import (quantit.adapter.core SubscriberAdapter OrderAdapter)))

;; TODO: Parameters should be changed to bars list (first bar is current one)
(defn update-system-state [trade-system bars update-statefn]
  (component/update-system
    trade-system
    (keys trade-system)
    (fn [component]
      (assoc component :state (update-statefn component bars)))))

(defn update-system-state-before [trade-system bars]
  (update-system-state trade-system bars update-state-before))

(defn update-system-state-after [trade-system bars]
  (update-system-state trade-system bars update-state-after))

;; TODO: Add some kind of capability to start with bar history older than system start or maybe even delegate the
;;        responsibility to the user to keep track of bar history through state
(defn run-trade-system [trade-system ^SubscriberAdapter subscriber ^OrderAdapter orderer]
  {:pre [(s/valid? :quantit.adapter/subscriber subscriber)
         (s/valid? :quantit.adapter/orderer orderer)]}
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
           bar-history '()]
      (let [bars (conj bar-history bar)]
        (if (not (end? bar))
          (let [trade-system (update-system-state-before trade-system bars)]
            (inspect bar)                                   ;; TODO: handle new incoming bar
            (recur (update-system-state-after trade-system bars)
                   (async/<!! barc)
                   (conj bar-history bar)))
          (do (async/>!! orderc bar)))))))
