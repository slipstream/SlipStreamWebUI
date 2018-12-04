(ns sixsq.slipstream.webui.deployment-dialog.views-summary
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.deployment-dialog.subs :as subs]
    [sixsq.slipstream.webui.history.views :as history]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))


(defn content
  []
  (let [deployment (subscribe [::subs/deployment])
        data-clouds (subscribe [::subs/data-clouds])
        selected-cloud (subscribe [::subs/selected-cloud])
        selected-credential (subscribe [::subs/selected-credential])
        connectors (subscribe [::subs/connectors])]
    (let [{:keys [name module]} @deployment
          {:keys [connector-id doc_count]} (first (filter #(= @selected-cloud (:key %)) @data-clouds))
          {cred-id          :id
           cred-name        :name
           cred-description :description} @selected-credential
          {connector-name        :name
           connector-description :description} (get @connectors connector-id)]

      [ui/Table
       [ui/TableBody
        [ui/TableRow
         [ui/TableCell "Application Name"]
         [ui/TableCell (or name (-> module :path (str/split #"/") last))]]
        [ui/TableRow
         [ui/TableCell "Application Path"]
         [ui/TableCell (:path module)]]
        (when cred-id
          [ui/TableRow
           [ui/TableCell "Credential"]
           [ui/TableCell (or cred-name (history/link (str "cimi/" cred-id) cred-id))
            (when cred-description
              [:br]
              [:p cred-description])]])
        (when connector-id
          [ui/TableRow
           [ui/TableCell "Selected Cloud"]
           [ui/TableCell (or connector-name (history/link (str "cimi/" connector-id) connector-id))
            (when connector-description
              [:br]
              [:p connector-description])]])
        (when doc_count
          [ui/TableRow
           [ui/TableCell "Number of Selected Objects"]
           [ui/TableCell doc_count]])
        ]])))
