(ns quantit.order.core)

(def order-types #{:buy :sell :buy-limit :sell-limit :buy-stop :sell-stop :buy-stop-limit :sell-stop-limit :close-by})

(def order-states #{:started :placed :canceled :partial :filled :rejected :expired :request-add :request-modify :request-cancel})

(def buy-sell {:buy  :sell
               :sell :buy})

(defprotocol Order
  (close [this])
  (same-type? [this order])
  (inverse? [this order])
  (merge-order [this order]))

(defrecord MarketOrder [direction amount]
  Order
  (close [this] (let [direction (-> this :direction buy-sell)]
                  (->MarketOrder direction (:amount this))))

  (same-type? [_ order] (instance? MarketOrder order))

  (inverse? [this order]
    (and (same-type? this order)
         (= (-> this :direction buy-sell) (:direction order))))

  (merge-order [this order]
    (cond
      (inverse? this order) (let [this-amount (:amount this)
                                  order-amount (:amount order)
                                  new-amount (- this-amount order-amount)]
                              (cond
                                ;; If new-amount is negative, then invert the direction
                                ;; and amount is absolute value of new-amount
                                (neg? new-amount) (->MarketOrder (buy-sell (:direction this)) (- new-amount))
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
      :else (ex-info "Trying to merge two orders of different type" {:a this :b order}))))

(defn market-order-buy [amount]
  (->MarketOrder :buy amount))

(defn market-order-sell [amount]
  (->MarketOrder :sell amount))

(defn volume [order]
  (condp = (:direction order)
    :buy (:amount order)
    :sell (- (:amount order))))

(defn order? [x]
  (satisfies? Order x))
