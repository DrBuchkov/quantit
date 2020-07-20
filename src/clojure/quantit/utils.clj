(ns quantit.utils)

(defn extended-indicator-form-args->map [x]
  (->> x
       rest
       (partition 2)
       (map vec)
       (into {})))
