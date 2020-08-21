(ns quantit.order.core
  (:require [tick.alpha.api :as t])
  (:import (order MarketOrder OrderDirection)))

(declare market-order-buy)
(declare market-order-sell)

(def buy-sell-java {:buy  OrderDirection/BUY
                    :sell OrderDirection/SELL})
(defprotocol Order
  (close [this])
  (same-type? [this order])
  (inverse? [this order])
  (merge-order [this order])
  (to-java [this]))

(defrecord MarketOrderClj [direction amount datetime]
  Order
  (close [this] (condp = (:direction this)
                  :buy (market-order-sell (:amount this))
                  :sell (market-order-buy (:amount this))))

  (same-type? [_ order] (instance? MarketOrderClj order))

  (inverse? [this order]
    (and (same-type? this order)
         (not= (-> this :direction) (:direction order))))

  (merge-order [this order]
    (cond
      (inverse? this order) (let [this-amount (:amount this)
                                  order-amount (:amount order)
                                  new-amount (- this-amount order-amount)]
                              (cond
                                ;; If new-amount is negative, then invert the direction
                                ;; and amount is absolute value of new-amount
                                (neg? new-amount) (condp = (:direction this)
                                                    :buy (market-order-sell (- new-amount))
                                                    :sell (market-order-buy (- new-amount)))
                                ;; If new-amount is zero, then the two orders cancel out.
                                (zero? new-amount) nil
                                ;; If new-amount is not negative and not zero, then return new order with
                                ;; amount equal to new-amount
                                :else (assoc this :amount new-amount)))
      ;; If the two orders are of same type and are not inverse, then the new order
      ;; is of the same type with amount equal to the sum of the two amounts
      (same-type? this order) (let [order-amount (:amount order)]
                                (update this :amount
                                        (fn [this-amount order-amount]
                                          (+ this-amount order-amount))
                                        order-amount))
      :else (ex-info "Trying to merge two orders of different type" {:a this :b order})))

  (to-java [_] (MarketOrder. (buy-sell-java direction) amount datetime)))

(defn market-order-buy [amount]
  (map->MarketOrderClj {:direction :buy
                        :amount    amount}))

(defn market-order-sell [amount]
  (map->MarketOrderClj {:direction :sell
                        :amount    amount}))

(defn order? [x]
  (satisfies? Order x))
