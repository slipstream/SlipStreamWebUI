(ns sixsq.slipstream.webui.panel.profile.views
  (:require
    [clojure.string :as str]
    [re-com.core :refer [v-box title]]
    [re-frame.core :refer [subscribe]]
    [sixsq.slipstream.webui.panel.profile.subs]))

(defn profile-panel
  []
  (let [tr (subscribe [:i18n-tr])
        user-id (subscribe [:user-id])]
    (fn []
      (let [profile-text (if @user-id
                           (@tr [:profile-text-logged-in] [@user-id])
                           (@tr [:profile-text-logged-out]))]
        [v-box
         :children [[title
                     :label (@tr [:profile])
                     :level :level1
                     :underline? true]
                    [:span profile-text]]]))))


