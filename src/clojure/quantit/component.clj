(ns quantit.component
  (:require [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]))

(s/def ::dep (s/tuple keyword? symbol?))

(s/def ::deps (s/coll-of ::dep :kind vector?))

(s/def ::name symbol?)

(defn class->kw [class]
  (csk/->kebab-case-keyword class))

(defn deps->map [deps]
  (into {} (mapv (fn [[first second]]
                   [first (class->kw second)])
                 deps)))

(defn deps->props [deps]
  (mapv (comp symbol name first) deps))

(defn constr-sym [name]
  (symbol (str "constr-" name)))

(defn map-constr-sym [name]
  (symbol (str "map->" name)))

(defn rec-constr-sym [name]
  (symbol (str "->" name)))

(defmacro defcomponent [name depsv type & body]
  {:pre [(s/valid? ::name name)
         (s/valid? ::deps depsv)]}
  (prn (namespace name))
  (let [props (deps->props depsv)
        map-constr (map-constr-sym name)
        constr (constr-sym name)]
    `(do
       (defrecord ~name ~props
         component/Lifecycle
         ~'(start [this] this)
         ;(~'start [~'this] (assoc ~'this :deps ~(deps->map depsv)))
         ~'(stop [this] this))
       (extend-type ~name
         ~type
         ~@body)
       (defn ~constr []
         ~(if (empty? depsv)
            `(~(rec-constr-sym name))
            `(component/using
               (~map-constr {})
               ~(deps->map depsv)))))))
