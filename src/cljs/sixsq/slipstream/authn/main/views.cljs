(ns sixsq.slipstream.authn.main.views
  (:require
    [re-com.core :refer [v-box alert-box title]]
    [re-frame.core :refer [subscribe dispatch]]
    [taoensso.timbre :as log]

    [sixsq.slipstream.authn.main.events]
    [sixsq.slipstream.authn.main.subs]
    [sixsq.slipstream.webui.panel.authn.views :as forms]
    [sixsq.slipstream.webui.panel.authn.views-session :as session]
    [reagent.core :as reagent]))

(defn redirect
  []
  (log/info "dispatching trigger-redirect")
  (dispatch [:evt.authn.main/trigger-redirect]))

(defn message-modal
  []
  (let [query-params (subscribe [:webui.main/nav-query-params])
        show? (reagent/atom true)]
    (fn []
      (let [{:keys [error]} @query-params]
        (when (and @show? error)
          [alert-box
           :alert-type :danger
           :heading "Login Error"
           :body error
           :closeable? true
           :on-close #(reset! show? false)])))))

(defn app
  []
  (fn []
    (let [session (subscribe [:webui.authn/session])]
      (fn []
        (when @session (redirect))
        [v-box
         :children [[message-modal]
                    (when @session [title
                                    :label "Redirecting..."
                                    :level :level1
                                    :underline? true])
                    (if @session
                      [session/session-info]
                      [forms/login-controls])]]))))
