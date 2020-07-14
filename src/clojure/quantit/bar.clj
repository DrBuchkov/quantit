(ns quantit.bar
  (:require [clojure.spec.alpha :as s]))

(s/def ::bar (s/keys :req-un [::open ::high ::low ::close ::volume]))

(s/def ::bar-series (s/coll-of ::bar :kind vector?))
