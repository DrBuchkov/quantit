(ns quantit.rule.spec
  (:require [quantit.rule.core :refer [Rule]]
            [clojure.spec.alpha :as s]))

(s/def :quantit.rule/name keyword?)

(s/def :quantit.rule/description string?)

(s/def :quantit.rule/satisfied-fn fn?)

(s/def :quantit.rule/rule #(satisfies? Rule %))