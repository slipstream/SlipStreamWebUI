(ns sixsq.slipstream.webui.deployment-dialog.views-summary
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.deployment-dialog.views-credentials :as credentials-step]
    [sixsq.slipstream.webui.deployment-dialog.views-data :as data-step]
    [sixsq.slipstream.webui.deployment-dialog.views-parameters :as parameters-step]
    [sixsq.slipstream.webui.deployment-dialog.views-size :as size-step]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn application-list-item
  []
  (let [deployment (subscribe [::subs/deployment])

        {:keys [name module]} @deployment
        header (or name (-> module :path (str/split #"/") last))
        description (:path module)]

    ^{:key "application"}
    [ui/ListItem {:active   false
                  :disabled true}
     [ui/ListIcon {:name "sitemap", :size "large", :vertical-align "middle"}]
     [ui/ListContent
      [ui/ListHeader header]
      (when description
        [ui/ListDescription description])]]))


(defn content
  []
  (let [data-step-active? (subscribe [::subs/data-step-active?])]
    [ui/ListSA {:divided   true
                :relaxed   true
                :selection true}
     [application-list-item]
     (when @data-step-active?
       [data-step/summary-item])
     [credentials-step/summary-item]
     [size-step/summary-item]
     [parameters-step/summary-item]]))
