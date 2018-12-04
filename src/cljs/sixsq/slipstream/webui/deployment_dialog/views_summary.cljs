(ns sixsq.slipstream.webui.deployment-dialog.views-summary
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.history.views :as history]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.deployment-dialog.spec :as spec]))


(defn application-list-item
  [_ {:keys [name module] :as deployment}]
  (let [header (or name (-> module :path (str/split #"/") last))
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
  (let [deployment (subscribe [::subs/deployment])
        step-states (subscribe [::subs/step-states])]

    (vec (concat [ui/ListSA {:divided   true
                             :relaxed   true
                             :selection true}
                  [application-list-item {} @deployment]]
                 (mapv (fn [step-id] (get-in @step-states [step-id :summary])) spec/steps)))))
