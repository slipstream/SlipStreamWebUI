(ns sixsq.slipstream.webui.data-detail.views
  (:require
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn detail
  [path]
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container
     [ui/Header {:as "h1"}
      (str (@tr [:data]) " detail")]]))
