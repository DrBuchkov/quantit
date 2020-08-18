(ns quantit.order.core)

(def order-types #{:buy :sell :buy-limit :sell-limit :buy-stop :sell-stop :buy-stop-limit :sell-stop-limit :close-by})

(def order-states #{:started :placed :canceled :partial :filled :rejected :expired :request-add :request-modify :request-cancel})

(def buy-sell {:buy  :sell
               :sell :buy})

(defprotocol Order
  (close [this])
  (inverse? [this order]))

(defrecord MarketOrder [direction amount]
  Order
  (close [this] (let [direction (-> this :direction buy-sell)]
                  (->MarketOrder direction (:amount this))))
  (inverse? [this order]
    (and (instance? MarketOrder order)
         (= (:amount this) (:amount order))
         (= (-> this :direction buy-sell) (:direction order)))))

(defn market-order-buy [amount]
  (->MarketOrder :buy amount))

(defn market-order-sell [amount]
  (->MarketOrder :sell amount))

(defn volume [^Order order]
  (condp = (:direction order)
    :buy (:amount order)
    :sell (- (:amount order))))

