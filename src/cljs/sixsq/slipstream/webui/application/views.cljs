(ns sixsq.slipstream.webui.application.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.application.events :as application-events]
    [sixsq.slipstream.webui.application.subs :as application-subs]
    [sixsq.slipstream.webui.editor.editor :as editor]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.component :as cutil]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [taoensso.timbre :as log]))


(defn category-icon
  [category]
  (case category
    "PROJECT" "folder"
    "APPLICATION" "sitemap"
    "IMAGE" "file"
    "COMPONENT" "microchip"
    "question circle"))


(defn format-module [{:keys [type name description] :as module}]
  (when module
    (let [on-click #(dispatch [::main-events/push-breadcrumb name])
          icon-name (category-icon type)]
      [ui/ListItem
       [ui/ListIcon {:name           icon-name
                     :size           "large"
                     :vertical-align "middle"}]
       [ui/ListContent
        [ui/ListHeader [:a {:on-click on-click} name]]
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


(defn more-or-less
  [state-atom]
  (let [tr (subscribe [::i18n-subs/tr])
        more? state-atom]
    (fn [state-atom]
      (let [label (if @more? (@tr [:less]) (@tr [:more]))
            icon-name (if @more? "caret down" "caret right")]
        [:a {:style    {:cursor "pointer"}
             :on-click #(reset! more? (not @more?))} [ui/Icon {:name icon-name}] label]))))


(defn format-meta
  [{:keys [name path version description logo type] :as module-meta}]
  (let [more? (reagent/atom false)]
    (fn [{:keys [name path version description logo type] :as module-meta}]
      (let [data (sort-by first (dissoc module-meta :versions :children :acl :operations))]
        (when (pos? (count data))
          [ui/Card {:fluid true}
           [ui/CardContent
            (when logo
              [ui/Image {:floated "right"
                         :size    :tiny
                         :src     (:href logo)}])
            [ui/CardHeader
             [ui/Icon {:name (category-icon type)}]
             (cond-> name
                     (not (str/blank? path)) (str " (" path ")"))]
            (when description
              [ui/CardMeta
               [:p description]])
            [ui/CardDescription
             [more-or-less more?]
             (when @more?
               [group-table-sui data])]]])))))


(defn error-text [tr error]
  (if-let [{:keys [status body reason]} (ex-data error)]
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


(defn dimmer
  []
  (let [tr (subscribe [::i18n-subs/tr])
        data (subscribe [::application-subs/module])]
    (fn []
      (let [loading? (not @data)]
        [ui/Dimmer {:active   loading?
                    :page     false
                    :inverted true}
         [ui/Header {:as       "h3"
                     :icon     true
                     :inverted false}
          [ui/Icon {:name    "refresh"
                    :loading true}]
          (@tr [:loading])]]))))


(defn format-module-children
  [module-children]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [module-children]
      (when (pos? (count module-children))
        [cc/collapsible-card
         (@tr [:modules])
         (vec (concat [ui/ListSA {:divided true
                                  :relaxed true}]
                      (map format-module module-children)))]))))


(defn parameter-table-row
  [[name description value]]
  [ui/TableRow
   [ui/TableCell name]
   [ui/TableCell description]
   [ui/TableCell value]])


(defn format-parameters
  [title-kw parameters]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [title-kw parameters]
      (when parameters
        (let [rows (map (fn [[k {:keys [description value]}]] [(name k) description value]) parameters)]
          [cc/collapsible-card
           (@tr [title-kw])
           [ui/Table
            (vec (concat [ui/TableBody] (map parameter-table-row rows)))]])))))


(defn target-dropdown
  [state]
  [ui/Dropdown {:inline        true
                :default-value :deployment
                :on-change     (cutil/callback :value #(reset! state %))
                :options       [{:key "preinstall", :value "preinstall", :text "pre-install"}
                                {:key "packages", :value "packages", :text "packages"}
                                {:key "postinstall", :value "postinstall", :text "post-install"}
                                {:key "deployment", :value "deployment", :text "deployment"}
                                {:key "reporting", :value "reporting", :text "reporting"}
                                {:key "onVmAdd", :value "onVmAdd", :text "on VM add"}
                                {:key "onVmRemove", :value "onVmRemove", :text "on VM remove"}
                                {:key "prescale", :value "prescale", :text "pre-scale"}
                                {:key "postscale", :value "postscale", :text "post-scale"}]}])


(defn render-package
  [package]
  ^{:key package}
  [ui/ListItem
   [ui/ListContent
    [ui/ListHeader package]]])


(defn render-packages
  [packages]
  (if (empty? packages)
    [:span "no packages defined"]
    (vec (concat [ui/ListSA] (mapv render-package packages)))))


(defn render-script
  [script]
  (if (str/blank? script)
    [:span "undefined"]
    [editor/editor (reagent/atom script) :options {:lineNumbers true, :readOnly true}]))


(defn format-targets
  [targets]
  (let [selected-target (reagent/atom "deployment")]
    (fn [targets]
      (when targets
        (let [selected (keyword @selected-target)
              target-value (get targets selected)]
          [cc/collapsible-card
           [:span [target-dropdown selected-target] "target"]
           [ui/Segment
            (if (= :packages selected)
              (render-packages target-value)
              (render-script target-value))]])))))


(defn format-component-link
  [label href]
  (let [on-click #(dispatch [::history-events/navigate (str "cimi/" href)])]
    [:a {:style {:cursor "pointer"} :on-click on-click} label]))


(defn render-parameter-mapping
  [[parameter {:keys [value mapped]}]]
  (let [parameter-name (name parameter)
        label (cond-> parameter-name
                      mapped (str " \u2192 ")
                      (not mapped) (str " \ff1d ")
                      value (str value)
                      (not value) (str "empty"))]
    ^{:key parameter-name}
    [ui/ListItem
     [ui/ListContent
      [ui/ListHeader label]]]))


(defn render-parameter-mappings
  [parameter-mappings]
  (if (empty? parameter-mappings)
    [:span "none"]
    (vec (concat [ui/ListSA] (mapv render-parameter-mapping (sort-by #(% first name) parameter-mappings))))))


(defn render-node
  [[node {:keys [multiplicity component parameterMappings] :as content}]]
  (let [label (name node)]
    [cc/collapsible-card
     [:span label]
     [ui/Table
      (vec (concat [ui/TableBody]
                   [[:tr [:td "component"] [:td (format-component-link label (:href component))]]
                    [:tr [:td "multiplicity"] [:td multiplicity]]
                    [:tr [:td "parameterMappings"] [:td (render-parameter-mappings parameterMappings)]]]))]]))


(defn format-nodes
  [nodes]
  (let [sorted-nodes (sort-by #(-> % first name) nodes)]
    (vec (concat [ui/Segment] (mapv render-node sorted-nodes)))))


(defn module-resource []
  (let [data (subscribe [::application-subs/module])]
    (fn []
      (let [loading? (not @data)]
        [ui/DimmerDimmable
         (vec (concat [ui/Container [dimmer]]
                      (when-not loading?
                        (if (instance? js/Error @data)
                          [[format-error @data]]
                          (let [{:keys [children content]} @data
                                metadata (dissoc @data :content)
                                {:keys [targets nodes inputParameters outputParameters]} content
                                type (:type metadata)]
                            (log/error (with-out-str (cljs.pprint/pprint nodes)))
                            [[format-meta metadata]
                             (when (= type "COMPONENT") [format-parameters :input-parameters inputParameters])
                             (when (= type "COMPONENT") [format-parameters :output-parameters outputParameters])
                             (when (= type "COMPONENT") [format-targets targets])
                             (when (= type "APPLICATION") [format-nodes nodes])
                             [format-module-children children]])))))]))))


(defmethod panel/render :application
  [path]
  (dispatch [::application-events/get-module])
  [module-resource])
