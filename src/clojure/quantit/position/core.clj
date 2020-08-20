(ns quantit.position.core
  (:require [quantit.order.core :as o])
  (:import (quantit.order.core Order)))


(defrecord Position [symbol orders])

(defn new-position
  [symbol]
  (map->Position {:symbol symbol
                  :orders []}))

(defn active? [^Position position]
  (> (count (:orders position)) 0))

(defn add-order [^Position position ^Order new-order]
  (if (zero? (count (:orders position)))
    (assoc position :orders [new-order])
    (assoc position :orders (loop [orders (:orders position)
                                   new-orders []]
                              (let [order (first orders)]
                                (if (o/same-type? order new-order)
                                  (let [merged-order (o/merge-order order new-order)]
                                    (if merged-order
                                      (concat (conj new-orders merged-order) (rest orders))
                                      ;; If merged-order is nil, then the two orders canceled out on merge
                                      (concat new-orders (rest orders))))
                                  (recur (rest orders) (conj new-orders order))))))))

(defn update-position [^Position position orders]
  (cond (or (vector? orders)
            (seq? orders)) (reduce add-order position orders)
        (o/order? orders) (add-order position orders)))

(defn close-position [^Position position]
  (->> (:orders position)
       (mapv o/close)))