(ns quantit.component-test
  (:require [midje.sweet :refer :all]
            [quantit.component :refer :all]
            [quantit.test-util :refer [fspec-fact]]))

(facts "Functions conform to their spec"
       (fspec-fact `constr-sym)
       (fspec-fact `map-constr-sym))
