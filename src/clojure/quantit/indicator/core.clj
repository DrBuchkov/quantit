(ns quantit.indicator.core
  (:require [com.stuartsierra.component :as component]
            [quantit.component.core :refer [defcomponent]]))

(defprotocol Indicator
  (value [this bars]))

(defn indicator? [x] (and (class? x)
                          (extends? Indicator x)))



;; TODO: Augment body to add default implementation for update-state (or any other) methods if not provided
(defmacro defindicator [name basis & body]
  `(defcomponent ~name ~basis Indicator ~@body))

(comment
  (defrecord Name [deps]
    component/Lifecycle
    (start [this] (assoc this :deps {}))
    (stop [this] (dissoc this :deps))))