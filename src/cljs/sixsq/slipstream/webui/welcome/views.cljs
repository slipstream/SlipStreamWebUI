(ns sixsq.slipstream.webui.welcome.views
  (:require
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defmethod panel/render :welcome
  [path]
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container {:textAlign "center"}
     [ui/Image {:src "/images/login_page_content_big_image.png"
                :centered true
                :size "big"}]
     [ui/Header {:as "h5"}
      [:span#release-version (str "SlipStream v")]]]))
