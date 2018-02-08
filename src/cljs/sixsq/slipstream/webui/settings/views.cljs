(ns sixsq.slipstream.webui.settings.views
  (:require
    [re-frame.core :refer [subscribe]]
    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]

    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn settings-info
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container
     [ui/Header {:as "h1"} (@tr [:settings])]]))


(defmethod panel/render :settings
  [path]
  [settings-info])
