(ns quantit.trade-system.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :quantit.trade-system/params map?)
(s/def :quantit.trade-system/init-state map?)
(s/def :quantit.trade-system/extended-indicator-form-args (s/keys :opt-un [:quantit.trade-system/init-state :quantit.trade-system/params]))
(s/def :quantit.trade-system/extended-indicator-form (s/cat :indicator-class symbol?
                                                            :alias-form (s/? (s/cat :->-kw #{:->}
                                                                                    :alias keyword?))
                                                            :params-form (s/? (s/cat :params-kw #{:params}
                                                                                     :params :quantit.trade-system/params))
                                                            :init-state-form (s/? (s/cat :init-state-kw #{:init-state}
                                                                                         :init-state :quantit.trade-system/init-state))))

(s/def :quantit.trade-system/indicator-form (s/or :basic symbol?
                                                  :extended :quantit.trade-system/extended-indicator-form))

(s/def :quantit.trade-system/indicator-forms (s/coll-of :quantit.trade-system/indicator-form :kind vector?))

(s/def :quantit.trade-system/extended-strategy-form (s/cat :strategy-class symbol?
                                                           :params-form (s/? (s/cat :params-kw #{:params}
                                                                                    :params :quantit.trade-system/params))
                                                           :init-state-form (s/? (s/cat :init-state-kw #{:init-state}
                                                                                        :init-state :quantit.trade-system/init-state))))
(s/def :quantit.trade-system/strategy-form (s/or :basic symbol?
                                                 :extended :quantit.trade-system/extended-strategy-form))

(s/def :quantit.trade-system/trade-system-declarations (s/cat :strategy (s/cat :k #{:strategy}
                                                                               :v :quantit.trade-system/strategy-form)
                                                              :params (s/? (s/cat :k #{:params}
                                                                                  :v :quantit.trade-system/params))
                                                              :init-state (s/? (s/cat :k #{:init-state}
                                                                                      :v :quantit.trade-system/init-state))
                                                              :indicators (s/cat :k #{:indicators}
                                                                                 :v :quantit.trade-system/indicator-forms)))