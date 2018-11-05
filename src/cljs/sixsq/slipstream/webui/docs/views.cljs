(ns sixsq.slipstream.webui.docs.views
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch subscribe]]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.docs.events :as events]
    [sixsq.slipstream.webui.history.events :as history-events]
    [sixsq.slipstream.webui.docs.subs :as subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.semantic-ui-extensions :as uix]
    [sixsq.slipstream.webui.utils.style :as style]
    [sixsq.slipstream.webui.docs-detail.views :as docs-details-view]
    [taoensso.timbre :as log]
    [sixsq.slipstream.webui.utils.general :as general-utils]))


#_(defn control-bar
    []
    (let [tr (subscribe [::i18n-subs/tr])]
      (dispatch [__quota-events/get-quotas])
      (fn []
        [:div
         [ui/Menu {:attached "top", :borderless true}
          [uix/MenuItemWithIcon
           {:name      (@tr [:refresh])
            :icon-name "refresh"
            :on-click  #(dispatch [__quota-events/get-quotas])}]]])))



(defn row-fn [{:keys [id] :as entry}]
  [ui/TableRow {:on-click #(dispatch [::history-events/navigate
                                      (str "documentation/" (general-utils/resource-id->uuid id))])}
   [ui/TableCell {:collapsing true} (:name entry)]
   [ui/TableCell {:style {:max-width     "150px"
                          :overflow      "hidden"
                          :text-overflow "ellipsis"}} (:description entry)]])


(defn documents-table
  []
  (let [tr (subscribe [::i18n-subs/tr])
        loading? (subscribe [::subs/loading?])
        documents (subscribe [::subs/documents])]
    [ui/Segment (merge style/basic
                       {:class-name "webui-x-autoscroll"
                        :loading    (or @loading? (nil? @documents))})

     (when @documents
       [:div
        [ui/Table
         {:compact     "very"
          :single-line true
          :padded      false
          :unstackable true
          :selectable  true}
         [ui/TableHeader
          [ui/TableRow
           [ui/TableHeaderCell (@tr [:name])]
           [ui/TableHeaderCell (@tr [:description])]]]
         (vec (concat [ui/TableBody]
                      (map row-fn (sort-by :name (vals @documents)))))]
        #_[:pre (with-out-str (cljs.pprint/pprint @documents))]]
       )]))




(defn documents-view
  []
  (dispatch [::events/get-documents])
  (fn []
    [ui/Container {:fluid true}
     #_[control-bar]
     [documents-table]]))


(defmethod panel/render :documentation
  [_]
  [documents-view])

(defn documentation-resource
  []
  (let [path (subscribe [::main-subs/nav-path])
        query-params (subscribe [::main-subs/nav-query-params])
        documents (subscribe [::subs/documents])]
    (fn []
      (let [n (count @path)
            children (case n
                       1 [[documents-view]]
                       2 [[docs-details-view/docs-detail
                           (get @documents (str "resource-metadata/"  (second @path)))]]
                       [[documents-view]])]
        (vec (concat [ui/Segment style/basic] children))))))


(defmethod panel/render :documentation
  [path]
  [documentation-resource])
