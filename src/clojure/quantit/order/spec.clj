(ns quantit.order.spec
  (:require [clojure.spec.alpha :as s]
            [quantit.order.core :refer [order-states order-types]]))

(s/def :quantit.order/order-state order-states)

(s/def :quantit.order/order-type order-types)
