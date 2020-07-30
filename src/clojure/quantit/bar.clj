(ns quantit.bar
  (:require [clojure.spec.alpha :as s]
            [tick.alpha.api :as t])
  (:import (java.time Instant)))

(defn datetime? [x] (instance? Instant x))

(s/def ::datetime datetime?)

(s/def ::bar (s/keys :req-un [::datetime ::open ::high ::low ::close ::volume ::adj-close]))

(s/def ::bar-series (s/coll-of ::bar :kind vector?))
