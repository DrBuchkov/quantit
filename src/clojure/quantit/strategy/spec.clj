(ns quantit.strategy.spec
  (:require [clojure.spec.alpha :as s]
            [quantit.strategy.core :refer [strategy?]]))

(s/def :quantit.strategy/strategy strategy?)