(ns sixsq.slipstream.webui.panel.profile.views
  (:require
    [clojure.string :as str]
    [re-com.core :refer [h-box v-box title label]]
    [re-frame.core :refer [subscribe]]

    [sixsq.slipstream.webui.widget.i18n.subs]
    [sixsq.slipstream.webui.widget.authn.subs]))

(def ^:const common-attrs #{:created :updated :resourceURI :properties :acl :operations})

(defn remove-common-attrs
  [m]
  (into {} (remove #(common-attrs (first %)) m)))

(defn column
  [vs cls]
  [v-box
   :class "webui-column"
   :children (doall (for [v vs] [label :class cls :label v]))])

(defn session-table [session]
  (let [data (sort (remove-common-attrs session))
        ks (map (comp name first) data)
        vs (map (comp str second) data)]
    [h-box
     :children [[column ks "webui-row-header"]
                [column vs ""]]]))

(defn session-info
  [session]
  (when session
    [v-box
     :children [[title
                 :label "Current Session"
                 :level :level2
                 :underline? true]
                [session-table session]]]))

(defn profile-panel
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      (let [profile-text (if @session
                           (@tr [:profile-text-logged-in] [(:username @session)])
                           (@tr [:profile-text-logged-out]))]
        [v-box
         :children [[title
                     :label (@tr [:profile])
                     :level :level1
                     :underline? true]
                    [:span profile-text]
                    [session-info @session]]]))))


