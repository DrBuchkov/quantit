(ns quantit.indicator
  (:require [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [quantit.component :refer [defcomponent]]))

(defprotocol Indicator
  (value [this bar history state])
  (update-state-before [this bar history state])
  (update-state-after [this bar history state]))

(defn indicator? [x] (and (class? x)
                          (extends? Indicator x)))

(s/def ::indicator indicator?)

(s/def ::indicators (s/coll-of indicator? :kind vector?))

;; TODO: Augment body to add default implementation for update-state (or any other) methods if not provided
(defmacro defindicator [name depsv & body]
  `(defcomponent ~name ~depsv Indicator ~@body))

(comment
  (defrecord Name [deps]
    component/Lifecycle
    (start [this] (assoc this :deps {}))
    (stop [this] (dissoc this :deps))))