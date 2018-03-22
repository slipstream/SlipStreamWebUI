(ns sixsq.slipstream.webui.legal.views
  (:require
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defmethod panel/render :legal
  [path]
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container {:textAlign "center"}
     [ui/Header {:as "h1"}
      (@tr [:legal])]]))

