(ns sixsq.slipstream.webui.runner
  (:require [cljs.test :as test]
            [doo.runner :refer-macros [doo-tests]]
            [sixsq.slipstream.webui.utils-test]
            [sixsq.slipstream.webui.panel.cimi.utils-test]
            [sixsq.slipstream.webui.widget.breadcrumbs.utils-test]))

(doo-tests
  'sixsq.slipstream.webui.utils-test
  'sixsq.slipstream.webui.panel.cimi.utils-test
  'sixsq.slipstream.webui.widget.breadcrumbs.utils-test
  )
