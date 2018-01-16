(ns cubic.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [cubic.core-test]))

(doo-tests 'cubic.core-test)
