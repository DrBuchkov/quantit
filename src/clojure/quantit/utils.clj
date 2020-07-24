(ns quantit.utils
  (:require [clojure.spec.alpha :as s]))

(defmacro jcall [obj & args]
  (let [ref (if (and (symbol? obj)
                     (instance? Class (eval obj)))
              (eval obj)
              obj)]
    `(. ~ref ~@args)))

(defn get-component-deps [component]
  {:pre (s/valid? class? component)}
  (filterv #(and (not= 'state %)
                 (not= 'params %))
           (-> component
               (.getMethod "getBasis" nil)
               (.invoke nil nil))))

(defn classname [class]
  (-> class
      .getSimpleName))

(defn flat-seq->map [x]
  (->> x (partition 2) (map vec) (into {})))

(defn inspect [body]
  (prn body)
  body)
