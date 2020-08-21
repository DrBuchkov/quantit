(ns quantit.execution.core
  (:require [quantit.component.core :refer [update-state-before update-state-after]]
            [quantit.strategy.core :refer [entry? exit? on-entry]]
            [quantit.rule.core :refer [satisfied?]]
            [quantit.utils :refer [end? vec-or-seq?]]
            [quantit.position.core :as p]
            [clojure.core.async :as async]
            [com.stuartsierra.component :as component])
  (:import (quantit.position.core Position)
           (adapter.impl BaseSubscriberAdapter BaseOrderAdapter)))

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
      (when-let [orders (if-not (p/active? position)
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
                            nil))]
        ;; Augment order with bar's datetime
        (let [datetime (:datetime (first bars))
              add-datetime (fn [order]
                             (assoc order :datetime datetime))]
          (if (vec-or-seq? orders)
            (apply add-datetime orders)
            (add-datetime orders)))))))

;; TODO: Add some kind of capability to start with bar history older than system start
(defn run-trade-system [trade-system symbol ^BaseSubscriberAdapter subscriber ^BaseOrderAdapter orderer]
  (let [barc (async/chan)
        orderc (async/chan)
        handler (handle-bars trade-system)
        send-order (fn [order]
                     (async/>!! orderc order))]
    ;; Start running Subscriber Adapter
    (async/go (._run_intern subscriber symbol barc))
    ;; Start running Order Adapter
    (async/go (._run_intern orderer symbol orderc))
    (loop [trade-system trade-system
           bar (async/<!! barc)
           bar-history '()
           position (p/new-position symbol)]
      (let [bars (conj bar-history bar)]
        (if (not (end? bar))
          (let [trade-system (update-system-state-before trade-system bars)
                order (handler bars position)]
            (when order (if (vec-or-seq? order)
                          (apply send-order order)
                          (send-order order)))
            (recur (update-system-state-after trade-system bars)
                   (async/<!! barc)
                   (conj bar-history bar)
                   (if order (p/update-position position order) position)))
          (do (prn "End")
              (send-order :end)))))))
