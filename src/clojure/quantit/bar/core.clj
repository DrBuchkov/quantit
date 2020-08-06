(ns quantit.bar.core
  (:import (java.time Instant)))

(defn datetime? [x] (instance? Instant x))


