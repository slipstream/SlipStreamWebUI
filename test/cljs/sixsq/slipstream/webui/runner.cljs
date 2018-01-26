(ns sixsq.slipstream.webui.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [sixsq.slipstream.webui.core-test]))

(doo-tests 'sixsq.slipstream.webui.core-test)

