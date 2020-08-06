(ns quantit.adapter.spec
  (:require [clojure.spec.alpha :as s]
            [quantit.adapter.core :refer [subscriber? orderer?]]))

(s/def :quantit.adapter/subscriber subscriber?)

(s/def :quantit.adapter/orderer orderer?)
