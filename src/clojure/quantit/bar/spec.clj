(ns quantit.bar.spec
  (:require [clojure.spec.alpha :as s]
            [quantit.bar.core :refer [datetime?]]))

(s/def :quantit.bar/datetime datetime?)

(s/def :quantit.bar/bar (s/keys :req-un [:quantit.bar/datetime ::open ::high ::low ::close ::volume ::adj-close]))

(s/def :quantit.bar/bar-series (s/coll-of :quantit.bar/bar :kind vector?))
