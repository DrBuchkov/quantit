(ns quantit.indicator
  (:require [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]))

(defprotocol Indicator (value [this bar history]))

(defn indicator? [x] (instance? Indicator x))

(s/def ::indicator indicator?)

(defn deps->map [deps]
  (into {} (mapv (comp (fn [[first second]]
                         `[~first '~second])
                       vec)
                 (partition 2 deps))))

(defmacro defindicator [name depsv & body]
  `(do
     (defrecord ~name ~'[deps]
       component/Lifecycle
       (~'start [~'this] (assoc ~'this :deps ~(deps->map depsv)))
       ~'(stop [this] (dissoc this :deps)))
     (extend-type ~name
       Indicator
       ~@body)))

(comment
  (defrecord Name [deps]
    component/Lifecycle
    (start [this] (assoc this :deps {}))
    (stop [this] (dissoc this :deps))))