(ns quantit.utils)


(defn classname [class]
  (-> class
      .getSimpleName))

(defn flat-seq->map [x]
  (->> x (partition 2) (map vec) (into {})))

(defn inspect [body]
  (prn body)
  body)
