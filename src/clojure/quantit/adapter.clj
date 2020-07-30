(ns quantit.adapter
  (:require [clojure.spec.alpha :as s])
  )


;; TODO: Maybe this should be a Java class
(defprotocol SubscriberAdapter
  (run-subscriber [this datac]))

(defn subscriber? [adapter]
  (satisfies? SubscriberAdapter adapter))

(s/def ::subscriber subscriber?)

(defprotocol OrderAdapter
  (run-orders [this orderc]))

(defn orderer? [adapter]
  (satisfies? OrderAdapter adapter))

(s/def ::orderer orderer?)
