(ns sixsq.slipstream.webui.main
  "Provides parameterized initialization script for the web application."
  (:require
    [reagent.core :as reagent]
    [re-frame.core :refer [dispatch dispatch-sync]]

    ;; must include these to ensure that they are not elided when doing
    ;; advanced optimizations with the Google Closure compiler.
    [sixsq.slipstream.webui.routes]
    [sixsq.slipstream.webui.panel.authn.events]
    [sixsq.slipstream.webui.main.events]
    [sixsq.slipstream.webui.main.subs]
    [sixsq.slipstream.webui.main.views]
    [sixsq.slipstream.webui.widget.history.events]))

(defn init
  "Parameterized hook to start the browser single page application.

  The slipstream-url parameter points to the SlipStream server to use. Note
  that most browsers will block URLs that aren't on the same server as the
  JavaScipt. You must turn off these protections to point to a third-party
  server, which is useful for debugging.

  The path-prefix defines the 'context' of the application within the hosting
  webserver. This must be provided for the internal routing and history
  features to work correctly."
  [slipstream-url path-prefix redirect-uri]
  (.log js/console "using slipstream server:" slipstream-url)
  (.log js/console "using path prefix:" path-prefix)
  (.log js/console "using login redirect:" redirect-uri)
  (dispatch-sync [:evt.webui.main/initialize-db])
  (dispatch-sync [:evt.webui.main/initialize-client slipstream-url])
  (dispatch-sync [:fetch-cloud-entry-point])
  (dispatch-sync [:evt.webui.history/initialize path-prefix])
  (dispatch-sync [:evt.webui.authn/initialize])
  (dispatch-sync [:evt.webui.authn/set-redirect-uri redirect-uri])
  (dispatch [:evt.webui.authn/check-session])
  (when-let [header-element (.getElementById js/document "webui-header")]
    (reagent/render [sixsq.slipstream.webui.main.views/header] header-element))
  (when-let [footer-element (.getElementById js/document "webui-footer")]
    (reagent/render [sixsq.slipstream.webui.main.views/footer] footer-element))
  (when-let [container-element (.getElementById js/document "webui-container")]
    (reagent/render [sixsq.slipstream.webui.main.views/app] container-element)))
