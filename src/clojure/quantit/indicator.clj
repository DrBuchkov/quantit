(ns quantit.indicator
  (:require [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [quantit.component :refer [defcomponent]]))

(defprotocol Indicator (value [this bar history]))

(defn indicator? [x] (instance? Indicator x))

(s/def ::indicator indicator?)

(defmacro defindicator [name depsv & body]
  `(defcomponent ~name ~depsv Indicator ~@body))

(comment
  (defrecord Name [deps]
    component/Lifecycle
    (start [this] (assoc this :deps {}))
    (stop [this] (dissoc this :deps))))