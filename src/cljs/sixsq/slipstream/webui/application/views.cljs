(ns sixsq.slipstream.webui.application.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]

    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.application.subs :as application-subs]
    [sixsq.slipstream.webui.application.events :as application-events]
    [sixsq.slipstream.webui.main.events :as main-events]))


(defn format-module [{:keys [category name version description] :as module}]
  (when module
    (let [on-click #(dispatch [::main-events/push-breadcrumb name])
          icon-name (case category
                      "Project" "folder"
                      "Deployment" "sitemap"
                      "Image" "microchip"
                      "refresh")]
      [ui/ListItem
       [ui/ListIcon {:name           icon-name
                     :size           "large"
                     :vertical-align "middle"}]
       [ui/ListContent
        [ui/ListHeader [:a {:on-click on-click} (str name " (" version ")")]]
        [ui/ListDescription [:span description]]]])))


(defn tuple-to-row [[v1 v2]]
  [ui/TableRow
   [ui/TableCell {:description true} (str v1)]
   [ui/TableCell (str v2)]])


(defn group-table-sui
  [group-data]
  (let [data (sort-by first group-data)]
    [ui/Table
     {:compact    true
      :definition true
      :padded     false
      :style      {:max-width "100%"}}
     (vec (concat [ui/TableBody]
                  (map tuple-to-row (map (juxt (comp name first) (comp str second)) data))))]))


(defn format-meta [module-meta]
  (let [data (sort-by first (dissoc module-meta :logoLink))]
    (when (pos? (count data))
      [ui/Card {:fluid true}
       [ui/CardContent
        (when-let [{:keys [logoLink]} module-meta]
          [ui/Image {:floated "right"
                     :size    "small"
                     :src     logoLink}])
        [ui/CardHeader (str (:shortName module-meta))]
        [ui/CardDescription
         [group-table-sui data]]]])))


(defn error-text [tr error]
  (if-let [{:keys [:status :body :reason]} (-> error ex-data)]
    (str (or (@tr [reason]) (name reason)) " (" status ")")
    (str error)))


(defn format-error
  [error]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [error]
      (let [reason-text (error-text tr error)]
        [ui/Container
         [ui/Header {:as   "h3"
                     :icon true}
          [ui/Icon {:name "warning sign"}]
          reason-text]]))))


(defn module-resource []
  (let [tr (subscribe [::i18n-subs/tr])
        data (subscribe [::application-subs/module])]
    (fn []
      (if @data
        (if (instance? js/Error @data)
          [ui/Container
           [format-error @data]]
          (let [module-meta (dissoc @data :children)
                module-children (:children @data)]
            [ui/Container
             [format-meta module-meta]
             (when (pos? (count module-children))
               (vec (concat [ui/ListSA {:divided true
                                        :relaxed true}]
                            (map format-module module-children))))]))
        [ui/Container
         [ui/Dimmer {:active true}
          [ui/Header {:as       "h3"
                      :icon     true
                      :inverted true}
           [ui/Icon {:name    "refresh"
                     :loading true}]
           (@tr [:loading])]]]))))


(defmethod panel/render :application
  [path]
  (dispatch [::application-events/get-module])
  [module-resource])
