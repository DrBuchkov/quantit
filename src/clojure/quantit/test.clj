(ns quantit.test
  (:require [quantit.strategy :refer [defstrategy]]
            [quantit.indicator :refer [defindicator]]
            [quantit.execution :refer [deftrader indicator-forms->map]]
            [com.stuartsierra.component :as component]))

(comment
  (defrecord MyLowerIndicator []
    Indicator
    (value [_ _ _] (rand-int 100)))

  (defrecord MyUpperIndicator []
    Indicator
    (value [_ _ _] (rand-int 100)))

  (defrecord MyIndicator [lower-indicator upper-indicator]
    Indicator
    (value [this {:keys [lower-indicator upper-indicator]} _]
      (- upper-indicator lower-indicator)))

  (defrecord MyStrategy [my-indicator]
    Strategy
    (entry? [this {:keys [open high low close volume my-indicator] :as input} _]
      (when (< 0 my-indicator)
        true))
    (on-entry [this _ _])
    (exit? [this {:keys [open high low close volume my-indicator]} _]
      (when (> 0 my-indicator)
        true))
    (on-exit [this _ _])
    (update? [_ _ _] false)
    (on-update [_ _ _]))

  (def execution
    (component/system-map
      :my-lower-indicator (->MyLowerIndicator)
      :my-upper-indicator (->MyUpperIndicator)
      :my-indicator (component/using
                      (map->MyIndicator {})
                      {:lower-indicator :my-lower-indicator
                       :upper-indicator :my-upper-indicator})
      :my-strategy (component/using
                     (map->MyStrategy {})
                     {:my-indicator :my-indicator}))))



(defindicator MyLowerIndicator []
  (value [this _ _ _] 10)
  (update-state-before [this _ _ state] state)
  (update-state-after [this _ _ state] state))

(defindicator MyUpperIndicator []
  (value [this _ _ _] 20)
  (update-state-before [this _ _ state] state)
  (update-state-after [this _ _ state] state))

(defindicator MyIndicator [:lower-indicator :upper-indicator]
  (value [this {:keys [lower-indicator upper-indicator] :as bar} _]
    (/ (+ upper-indicator
          lower-indicator)
       2))
  (update-state-before [this _ _ state] state)
  (update-state-after [this _ _ state] state))

(defstrategy MyStrategy [:my-indicator]
  (entry? [this {:keys [open high low close volume my-indicator] :as input} _ _]
    (when (< 0 my-indicator)
      true))
  (on-entry [this {:keys [open high low close volume]} _ _])
  (exit? [this {:keys [open high low close volume my-indicator]}]
    (when (> 0 my-indicator)
      true))
  (on-exit [this {:keys [open high low close volume]} _ _])
  (update? [_ _ _ _] false)
  (on-update [_ _ _ _])
  (update-state-before [this _ _ state] state)              ;; Updates state before handling bar
  (update-state-after [this _ _ state] state))              ;; Update state after handling bar

(deftrader trader
  :strategy MyStrategy
  :indicators [MyIndicator
               [MyLowerIndicator :as :lower-indicator {:params     {:something 1}
                                                       :init-state {:my-state 0}}]
               [MyUpperIndicator :as :upper-indicator]])