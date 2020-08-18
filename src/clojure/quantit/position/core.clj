(ns quantit.position.core
  (:require [quantit.order.core :as o]))


(defrecord Position [symbol orders])

(defn active? [^Position position]
  (not= 0 (:volume position)))

(defn volume [^Position position]
  (let [orders (:orders position)]
    (->> orders
         (mapv (fn [order] (o/volume order))))))
