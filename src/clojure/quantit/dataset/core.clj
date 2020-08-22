(ns quantit.dataset.core
  (:require [clojure.core.matrix.dataset :as ds]))

(defn mapseq->ds [seq]
  (ds/dataset (keys (first seq)) seq))
