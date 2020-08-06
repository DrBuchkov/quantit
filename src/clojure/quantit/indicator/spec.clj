(ns quantit.indicator.spec
  (:require [clojure.spec.alpha :as s]
            [quantit.indicator.core :refer [indicator?]]))

(s/def :quantit.indicator/indicator indicator?)

(s/def :quantit.indicator/indicators (s/coll-of :quantit.indicator/indicator :kind vector?))
