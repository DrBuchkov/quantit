(ns quantit.order
  (:require [clojure.spec.alpha :as s]))

(def order-types #{:buy :self :buy-limit :sell-limit :buy-stop :sell-stop :buy-stop-limit :sell-stop-limit :close-by})

(s/def ::order-type order-types)

(def order-states #{:started :placed :canceled :partial :filled :rejected :expired :request-add :request-modify :request-cancel})

(s/def ::order-state order-states)

(defrecord Order [order-type order-state])
