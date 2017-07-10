(ns sixsq.slipstream.webui.panel.authn.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [re-com.core :refer [v-box title]]
    [sixsq.slipstream.webui.panel.authn.effects]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.panel.authn.subs]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]
    [sixsq.slipstream.webui.panel.authn.views-forms :as forms]
    [sixsq.slipstream.webui.resource :as resource]))

(defn login-resource
  "This panel shows the login controls if there is no active user session;
   otherwise it shows the details of the session."
  []
  (let [tr (subscribe [:webui.i18n/tr])
        session (subscribe [:webui.authn/session])]
    (fn []
      [v-box
       :children [[title
                   :label (@tr (if @session [:session] [:login]))
                   :level :level1
                   :underline? true]
                  (if @session
                    [session/session-info]
                    [forms/login-controls])]])))

(defmethod resource/render "login"
  [path query-params]
  (dispatch [:set-login-path-and-error (:error query-params)])
  [login-resource])
