(ns quantit.execution.core
  (:require [quantit.adapter.core :refer [run-subscriber handle-order! update-before update-after]]
            [quantit.component.core :refer [update-state-before update-state-after]]
            [quantit.strategy.core :refer [entry? exit? on-entry]]
            [quantit.rule.core :refer [satisfied?]]
            [quantit.utils :refer [end? inspect]]
            [quantit.position.core :as p]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component])
  (:import (quantit.adapter.core SubscriberAdapter OrderAdapter)
           (quantit.position.core Position)))

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

(defn handle-bars [trade-system]
  (let [strategy (:strategy trade-system)
        entry-rule (entry? strategy)
        exit-rule (exit? strategy)]
    (fn [bars ^Position position]
      (if-not (p/active? position)
        ;; No current position
        (if-let [satisfied (satisfied? entry-rule bars)]
          (do (prn "Enter")
              (on-entry strategy {:bars      bars
                                  :satisfied satisfied}))
          ;; do nothing
          nil)
        ;; Position already open
        (if (satisfied? exit-rule bars)
          ;; todo: close position here
          (do (prn "Exit")
              (p/close-position position))
          ;; do nothing
          nil)))))



;; TODO: Add some kind of capability to start with bar history older than system start
(defn run-trade-system [trade-system symbol ^SubscriberAdapter subscriber ^OrderAdapter orderer]
  {:pre [(s/valid? :quantit.adapter/subscriber subscriber)
         (s/valid? :quantit.adapter/orderer orderer)]}
  (let [barc (async/chan)
        orderc (async/chan)
        handler (handle-bars trade-system)]
    (async/go (run-subscriber subscriber barc))
    (async/go (loop [orderer orderer
                     order (async/<! orderc)]
                (when (not (end? order))
                  (let [orderer (update-before orderer order)]
                    (handle-order! orderer order)
                    (recur (update-after orderer order) (async/<! orderc))))))
    (loop [trade-system trade-system
           bar (async/<!! barc)
           bar-history '()
           position (p/new-position symbol)]
      (let [bars (conj bar-history bar)]
        (if (not (end? bar))
          (let [trade-system (update-system-state-before trade-system bars)
                order (handler bars position)]              ;; TODO: Cancel out position when close
            (recur (update-system-state-after trade-system bars)
                   (async/<!! barc)
                   (conj bar-history bar)
                   (if order (p/update-position position order) position)))
          (do (async/>!! orderc 'end)))))))
