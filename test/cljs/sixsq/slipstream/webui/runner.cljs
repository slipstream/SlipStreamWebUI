(ns sixsq.slipstream.webui.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [sixsq.slipstream.webui.utils.general-test]))

(doo-tests 'sixsq.slipstream.webui.utils.general-test)

