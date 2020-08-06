(ns quantit.adapter.core)


;; TODO: Maybe this should be a Java class
(defprotocol SubscriberAdapter
  (run-subscriber [this datac]))

(defn subscriber? [adapter]
  (satisfies? SubscriberAdapter adapter))

(defprotocol OrderAdapter
  (handle-order! [this order])
  (update-before [this order])
  (update-after [this order]))

(defn orderer? [adapter]
  (satisfies? OrderAdapter adapter))
