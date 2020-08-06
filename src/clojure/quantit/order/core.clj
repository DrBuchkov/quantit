(ns quantit.order.core)

(def order-types #{:buy :self :buy-limit :sell-limit :buy-stop :sell-stop :buy-stop-limit :sell-stop-limit :close-by})

(def order-states #{:started :placed :canceled :partial :filled :rejected :expired :request-add :request-modify :request-cancel})

(defrecord Order [order-type order-state])
