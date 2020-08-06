(ns quantit.backtest.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :quantit.backtest/interval #{:daily :weekly :monthly})
