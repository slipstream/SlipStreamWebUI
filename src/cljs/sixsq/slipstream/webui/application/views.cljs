(ns sixsq.slipstream.webui.application.views
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as reagent]
    [sixsq.slipstream.webui.application.events :as events]
    [sixsq.slipstream.webui.application.subs :as subs]
    [sixsq.slipstream.webui.application.utils :as utils]
    [sixsq.slipstream.webui.cimi.subs :as cimi-subs]
    [sixsq.slipstream.webui.deployment-dialog.events :as deployment-dialog-events]
    [sixsq.slipstream.webui.deployment-dialog.views :as deployment-dialog-views]
    [sixsq.slipstream.webui.history.views :as history]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.events :as main-events]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]
    [sixsq.slipstream.webui.utils.resource-details :as resource-details]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.utils.ui-callback :as ui-callback]
    [taoensso.timbre :as log]))


(defn refresh-button
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn []
      [ui/MenuMenu {:position "right"}
       [uix/MenuItemWithIcon
        {:name      (@tr [:refresh])
         :icon-name "refresh"
         :loading?  false                                   ;; FIXME: Add loading flag for module.
         :on-click  #(dispatch [::events/get-module])}]])))


(defn control-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        module (subscribe [::subs/module])
        cep (subscribe [::cimi-subs/cloud-entry-point])]
    (let [add-disabled? (not= "PROJECT" (:type @module))
          deploy-disabled? (= "PROJECT" (:type @module))]
      (vec (concat [ui/Menu {:borderless true}]

                   (resource-details/format-operations nil @module (:baseURI @cep) {})

                   [[uix/MenuItemWithIcon
                     {:name      (@tr [:launch])
                      :icon-name "rocket"
                      :disabled  deploy-disabled?
                      :on-click  #(dispatch [::deployment-dialog-events/create-deployment (:id @module) :credentials])}]

                    [uix/MenuItemWithIcon
                     {:name      (@tr [:add])
                      :icon-name "add"
                      :disabled  add-disabled?
                      :on-click  #(dispatch [::events/open-add-modal])}]
                    [refresh-button]])))))


(defn form-input-callback
  [path]
  (ui-callback/value #(dispatch [::events/update-add-data path %])))


(defn project-pane
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name description] :as project-data} :project} @add-data]
      ^{:key "project-pane"}
      [ui/TabPane
       [ui/Form {:id "add-project"}
        [ui/FormInput {:label     "name"
                       :value     (or name "")
                       :on-change (form-input-callback [:project :name])}]
        [ui/FormInput {:label     "description"
                       :value     (or description "")
                       :on-change (form-input-callback [:project :description])}]]])))

(defn general-pane
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name description] :as project-data} :project} @add-data]
      ^{:key "general-pane"}
      [ui/TabPane
       [ui/Form
        [ui/FormInput {:label     "name"
                       :value     (or name "")
                       :on-change (form-input-callback [:project :name])}]
        [ui/FormInput {:label     "description"
                       :value     (or description "")
                       :on-change (form-input-callback [:project :description])}]
        [ui/FormField
         [:label "parent module"]
         [ui/Input {:action {:icon "folder open", :on-click #()}}]

         ]]])))


(defn resource-pane
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name description] :as project-data} :project} @add-data]
      ^{:key "general-pane"}
      [ui/TabPane
       [ui/Form
        [ui/FormInput {:label "CPUs"}]
        [ui/FormField
         [:label "RAM"]
         [ui/Input {:label "MB", :label-position "right"}]
         ]
        [ui/FormField
         [:label "DISK"]
         [ui/Input {:label "GB", :label-position "right"}]]]])))


(defn recipes-pane
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name description] :as project-data} :project} @add-data]
      [ui/TabPane
       [ui/Dropdown {:inline        true
                     :default-value :deployment
                     :options       [{:key "preinstall", :value "preinstall", :text "pre-install"}
                                     {:key "packages", :value "packages", :text "packages"}
                                     {:key "postinstall", :value "postinstall", :text "post-install"}
                                     {:key "deployment", :value "deployment", :text "deployment"}
                                     {:key "reporting", :value "reporting", :text "reporting"}
                                     {:key "onVmAdd", :value "onVmAdd", :text "on VM add"}
                                     {:key "onVmRemove", :value "onVmRemove", :text "on VM remove"}
                                     {:key "prescale", :value "prescale", :text "pre-scale"}
                                     {:key "postscale", :value "postscale", :text "post-scale"}]}]

       [ui/Segment
        [ui/CodeMirror {:value   "#!/bin/bash -xe\n\n/opt/slipstream/client/sbin/slipstream.setenv && /opt/slipstream/bin/link-data.py\n\ntoken=$(ss-random -s 20)\n(cd /gssc && jupyter lab --ip=0.0.0.0 --allow-root --no-browser --NotebookApp.token=$token) 2>&1 >/var/log/slipstream/client/jupyter.log &\n\nip=$(ss-get hostname)\nport=$(ss-get port)\nport_published=$(ss-get \"tcp.$port\") || $port\nurl=\"http://$ip:$port_published/?token=$token\"\n\nss-set ss:url.service $url\n\nss-set url.service $url\n\njupyter notebook list\n\n"
                        :options {:line-numbers        true
                                  :match-brackets      true
                                  :auto-close-brackets true
                                  :style-active-line   true
                                  :fold-gutter         true
                                  :gutters             ["CodeMirror-foldgutter"]}}]]])))

(defn data-pane
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name description] :as project-data} :project} @add-data]
      [ui/TabPane
       [ui/Message {:info true}
        [ui/Icon {:name "pin"}]
        "Choose supported datasets types by the application"]
       [ui/Form
        [ui/FormInput {:value "application/x-sdrData"}]
        [ui/FormInput {:value "application/x-ionMessage"}]
        [ui/FormField
         [ui/Button {:icon "plus", :basic true}]]]

       ]))
  )

(defn image-pane
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name
                   description
                   connector
                   image-id
                   author
                   loginUser
                   networkType
                   os] :as image-data} :image} @add-data]
      ^{:key "image-pane"}
      [ui/TabPane
       [ui/Form {:id "add-image"}
        [ui/FormInput {:label     "name"
                       :value     (or name "")
                       :on-change (form-input-callback [:image :name])}]
        [ui/FormInput {:label     "description"
                       :value     (or description "")
                       :on-change (form-input-callback [:image :description])}]
        [ui/FormInput {:label     "connector"
                       :value     (or connector "")
                       :on-change (form-input-callback [:image :connector])}]
        [ui/FormInput {:label     "image ID"
                       :value     (or image-id "")
                       :on-change (form-input-callback [:image :image-id])}]
        [ui/FormInput {:label     "author"
                       :value     (or author "")
                       :on-change (form-input-callback [:image :author])}]
        [ui/FormInput {:label     "loginUser"
                       :value     (or loginUser "")
                       :on-change (form-input-callback [:image :loginUser])}]
        [ui/FormInput {:label     "networkType"
                       :value     (or networkType "")
                       :on-change (form-input-callback [:image :networkType])}]
        [ui/FormInput {:label     "os"
                       :value     (or os "")
                       :on-change (form-input-callback [:image :os])}]
        ]])))


(defn component-pane
  []
  ^{:key "component-pane"}
  [ui/TabPane
   [ui/Form {:id "add-component"}
    [ui/FormInput {:label "name"}]
    [ui/FormInput {:label "description"}]]])


(defn application-pane
  []
  ^{:key "application-pane"}
  [ui/TabPane
   [ui/Form {:id "add-application"}
    [ui/FormInput {:label "name"}]
    [ui/FormInput {:label "description"}]]])


(defn kw->icon-name
  [kw]
  (-> kw name str/upper-case utils/category-icon))


(defn pane
  [tr kw icon element]
  {:menuItem {:key     (name kw)
              :icon    icon
              :content (@tr [kw])}
   :render   (fn [] (reagent/as-element [element]))})


(defn index->kw
  [index]
  (case index
    0 :project
    1 :image
    2 :component
    3 :application
    :project))


(defn application-tab-index->kw
  [index]
  (case index
    0 :general
    1 :image
    2 :component
    3 :application
    :project))



(defn add-form-select-project-image-app
  []
  (let [tr (subscribe [::i18n-subs/tr])
        render-fn (fn [kw]
                    [ui/MenuItem {:name     (name kw)
                                  :on-click #(dispatch [::events/set-add-modal-step kw])}
                     [ui/Icon {:name (kw->icon-name kw)}]
                     (@tr [kw])])]

    (vec (concat [ui/Menu {:fluid true, :widths 3, :icon "labeled"}]
                 (map render-fn [:project, :image, :application])))))

(defn add-form-create-project
  []
  (let [add-data (subscribe [::subs/add-data])
        {{:keys [name description] :as project-data} :project} @add-data]
    [ui/Form {:id "add-project"}
     [ui/FormInput {:label     "name"
                    :value     (or name "")
                    :on-change (form-input-callback [:project :name])}]
     [ui/FormInput {:label     "description"
                    :value     (or description "")
                    :on-change (form-input-callback [:project :description])}]]))

(defn add-form-create-image
  []
  (let [add-data (subscribe [::subs/add-data])]
    (let [{{:keys [name
                   description
                   connector
                   image-id
                   author
                   loginUser
                   networkType
                   os] :as image-data} :image} @add-data]
      [ui/Form {:id "add-image"}
       [ui/FormInput {:label     "name"
                      :value     (or name "")
                      :on-change (form-input-callback [:image :name])}]
       [ui/FormInput {:label     "description"
                      :value     (or description "")
                      :on-change (form-input-callback [:image :description])}]
       [ui/FormInput {:label     "connector"
                      :value     (or connector "")
                      :on-change (form-input-callback [:image :connector])}]
       [ui/FormInput {:label     "image ID"
                      :value     (or image-id "")
                      :on-change (form-input-callback [:image :image-id])}]
       [ui/FormInput {:label     "author"
                      :value     (or author "")
                      :on-change (form-input-callback [:image :author])}]
       [ui/FormInput {:label     "loginUser"
                      :value     (or loginUser "")
                      :on-change (form-input-callback [:image :loginUser])}]
       [ui/FormInput {:label     "networkType"
                      :value     (or networkType "")
                      :on-change (form-input-callback [:image :networkType])}]
       [ui/FormInput {:label     "os"
                      :value     (or os "")
                      :on-change (form-input-callback [:image :os])}]
       ])))

(defn add-form-create-application
  []
  (let [add-data (subscribe [::subs/add-data])
        tr (subscribe [::i18n-subs/tr])]
    (let [{{:keys [name
                   description
                   connector
                   image-id
                   author
                   loginUser
                   networkType
                   os] :as image-data} :image} @add-data]
      [ui/Tab
       {:panes         [(pane tr :general "info" general-pane)
                        (pane tr :resources "microchip" resource-pane)
                        (pane tr :networking "world" general-pane)
                        (pane tr :parameters "bars" general-pane)
                        (pane tr :recipes "code" recipes-pane)
                        (pane tr :data "database" data-pane)]
        :on-tab-change (ui-callback/callback :activeIndex
                                             (fn [index]
                                               (let [kw (application-tab-index->kw index)]
                                                 (dispatch [::events/set-active-tab-application kw]))))}]
      #_[ui/Form {:id "add-application"}
         [ui/FormInput {:label     "name"
                        :value     (or name "")
                        :on-change (form-input-callback [:image :name])}]
         [ui/FormInput {:label     "description"
                        :value     (or description "")
                        :on-change (form-input-callback [:image :description])}]
         ])))


(defn add-modal-content
  []
  (let [nav-path (subscribe [::main-subs/nav-path])
        step (subscribe [::subs/add-modal-step])]
    (fn []
      [ui/ModalContent
       (if (= @step :select)
         [ui/Message {:info true}
          [ui/Icon {:name "pin"}]
          "Select one of following types"]
         [ui/Header {:as "h3"}
          [ui/Icon {:name (kw->icon-name @step)}]
          [ui/HeaderContent (str "/" (utils/nav-path->module-path @nav-path))]])

       (case @step
         :select [add-form-select-project-image-app]
         :project [add-form-create-project]
         :image [add-form-create-image]
         :application [add-form-create-application]
         [add-form-select-project-image-app])])))

(defn add-modal
  []
  (let [tr (subscribe [::i18n-subs/tr])
        visible? (subscribe [::subs/add-modal-visible?])]

    (fn []
      (let [hide-fn #(dispatch [::events/close-add-modal])
            submit-fn #(dispatch [::events/add-module])]
        [ui/Modal {:open       @visible?
                   :close-icon true
                   :on-close   hide-fn}

         [ui/ModalHeader [ui/Icon {:name "add"}] (@tr [:add])]
         [add-modal-content]
         #_[ui/ModalContent {:scrolling true}
            [ui/Header {:as "h3"} (utils/nav-path->module-path @nav-path)]
            (vec (concat [ui/Menu {:fluid true, :widths 3, :icon "labeled"}]
                         (map (fn [[name icon on-click-fn]]
                                [ui/MenuItem {:name name, :on-click on-click-fn}
                                 [ui/Icon {:name icon}]
                                 (@tr [(keyword name)])])
                              [["project" "folder" #()]
                               ["image" "file" #()]
                               ["application" "sitemap" #()]])))
            #_[ui/Menu {:fluid true, :widths 3, :icon "labeled"}
               [ui/MenuItem {:name "project", :on-click #()}
                [ui/Icon {:name "folder"}]
                (@tr [:project])]
               [ui/MenuItem {:name "image"}
                [ui/Icon {:name "file"}]
                (@tr [:image])]
               [ui/MenuItem {:name "application"}
                [ui/Icon {:name "microchip"}]
                (@tr [:application])]]
            #_[ui/Tab
               {:panes         [(pane tr :project project-pane)
                                (pane tr :image image-pane)
                                #_(pane tr :component component-pane)
                                #_(pane tr :application application-pane)]
                :on-tab-change (ui-callback/callback :activeIndex
                                                     (fn [index]
                                                       (let [kw (index->kw index)]
                                                         (dispatch [::events/set-active-tab kw]))))}]]

         [ui/ModalActions
          [uix/Button {:text (@tr [:cancel]), :on-click hide-fn}]
          [uix/Button {:text (@tr [:add]), :positive true, :on-click submit-fn}]]]))))


(defn format-module [{:keys [type name description] :as module}]
  (when module
    (let [on-click #(dispatch [::main-events/push-breadcrumb name])
          icon-name (utils/category-icon type)]
      [ui/ListItem {:on-click on-click}
       [ui/ListIcon {:name           icon-name
                     :size           "large"
                     :vertical-align "middle"}]
       [ui/ListContent
        [ui/ListHeader [:a {:on-click on-click} name]]
        [ui/ListDescription [:span description]]]])))


(defn tuple-to-row [[v1 v2]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (str v1)]
   [ui/TableCell (str v2)]])


(defn preprocess-metadata
  [{:keys [name path description logoURL type acl] :as module-meta}]
  {:title       name
   :subtitle    path
   :description description
   :logo        logoURL
   :icon        (utils/meta-category-icon type)
   :acl         acl})


(defn metadata-rows
  [module-meta]
  (->> (dissoc module-meta :versions :children :acl :operations)
       (map (juxt (comp name first) (comp str second)))
       (map tuple-to-row)))


(defn format-meta
  [module-meta]
  (let [metadata (preprocess-metadata module-meta)
        rows (metadata-rows module-meta)]
    [cc/metadata metadata rows]))


(defn error-text [tr error]
  (if-let [{:keys [status reason]} (ex-data error)]
    (str (or (@tr [reason]) (name reason)) " (" status ")")
    (str error)))


(defn format-error
  [error]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [error]
      (when (instance? js/Error error)
        [ui/Container
         [ui/Header {:as "h3", :icon true}
          [ui/Icon {:name "warning sign"}]
          (error-text tr error)]]))))


(defn format-module-children
  [module-children]
  (when (pos? (count module-children))
    [ui/Segment style/basic
     (vec (concat [ui/ListSA {:divided   true
                              :relaxed   true
                              :selection true}]
                  (map (fn [{:keys [id] :as module}]
                         ^{:key id}
                         [format-module module]) module-children)))]))


(defn parameter-table-row
  [[name description value]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} name]
   [ui/TableCell description]
   [ui/TableCell value]])


(defn format-parameters
  [title-kw parameters]
  (let [tr (subscribe [::i18n-subs/tr])]
    (fn [title-kw parameters]
      (when parameters
        (let [rows (mapv (juxt :parameter :description :value) parameters)]
          [cc/collapsible-segment
           (@tr [title-kw])
           [ui/Table style/definition
            (vec (concat [ui/TableBody] (map parameter-table-row rows)))]])))))


(defn target-dropdown
  [state]
  [ui/Dropdown {:inline        true
                :default-value :deployment
                :on-change     (ui-callback/value #(reset! state %))
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
    [ui/CodeMirror {:value   script
                    :options {:line-numbers true
                              :read-only    true}}]))


(defn format-targets
  [targets]
  (let [selected-target (reagent/atom "deployment")]
    (fn [targets]
      (when targets
        (let [selected (keyword @selected-target)
              target-value (get targets selected)]
          [cc/collapsible-segment
           [:span [target-dropdown selected-target] "target"]
           [ui/Segment
            (if (= :packages selected)
              (render-packages target-value)
              (render-script target-value))]])))))


(defn format-component-link
  [label href]
  [history/link (str "cimi/" href) label])


(defn render-parameter-mapping
  [{:keys [parameter value mapped]}]
  (let [label (str parameter
                   (if mapped " \u2192 " " \uff1d ")
                   (or value "empty"))]
    ^{:key parameter}
    [ui/ListItem
     [ui/ListContent
      [ui/ListHeader label]]]))


(defn render-parameter-mappings
  [parameter-mappings]
  (if (empty? parameter-mappings)
    [:span "none"]
    (vec (concat [ui/ListSA] (mapv render-parameter-mapping (sort-by :parameter parameter-mappings))))))


(defn render-node
  [{:keys [node multiplicity component parameterMappings] :as content}]
  (let [label (name node)]
    [cc/collapsible-segment
     [:span label]
     [ui/Table style/definition
      [ui/TableBody
       [ui/TableRow
        [ui/TableCell {:collapsing true} "component"]
        [ui/TableCell (format-component-link label (:href component))]]
       [ui/TableRow
        [ui/TableCell {:collapsing true} "multiplicity"]
        [ui/TableCell multiplicity]]
       [ui/TableRow
        [ui/TableCell {:collapsing true} "parameterMappings"]
        [ui/TableCell (render-parameter-mappings parameterMappings)]]]]]))


(defn format-nodes
  [nodes]
  (let [sorted-nodes (sort-by :node nodes)]
    (vec (concat [ui/Segment] (mapv render-node sorted-nodes)))))


(defn module-resource []
  (let [data (subscribe [::subs/module])]
    (fn []
      (vec (concat [ui/Container {:fluid true}
                    [control-bar]
                    [add-modal]
                    [deployment-dialog-views/deploy-modal false]
                    [format-error @data]]
                   (when (and @data (not (instance? js/Error @data)))
                     (let [{:keys [children content]} @data
                           metadata (dissoc @data :content)
                           {:keys [targets nodes inputParameters outputParameters]} content
                           type (:type metadata)]
                       [[format-meta metadata]
                        (when (= type "COMPONENT") [format-parameters :input-parameters inputParameters])
                        (when (= type "COMPONENT") [format-parameters :output-parameters outputParameters])
                        (when (= type "COMPONENT") [format-targets targets])
                        (when (= type "APPLICATION") [format-nodes nodes])
                        [format-module-children children]])))))))


(defmethod panel/render :application
  [path]
  (dispatch [::events/get-module])
  [module-resource])
