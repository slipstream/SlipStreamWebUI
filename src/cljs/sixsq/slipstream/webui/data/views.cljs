(ns sixsq.slipstream.webui.data.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]

    [sixsq.slipstream.webui.data.events :as data-events]
    [sixsq.slipstream.webui.data.subs :as data-subs]
    [sixsq.slipstream.webui.data-detail.views :as data-detail-views]
    [sixsq.slipstream.webui.dnd.views :as dnd-views]
    [sixsq.slipstream.webui.panel :as panel]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]
    [sixsq.slipstream.webui.main.subs :as main-subs]
    [sixsq.slipstream.webui.messages.events :as messages-events]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [sixsq.slipstream.webui.utils.response :as response]))


(defn results-display
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container
     [dnd-views/file-select #(js/alert (str "SELECT: " (dnd-views/file-stats %)))]
     [dnd-views/drop-zone #(js/alert (str "DROP: " (dnd-views/file-stats %)))]]))


(defn menu-bar []
  (let [tr (subscribe [::i18n-subs/tr])
        resources (subscribe [::data-subs/collection])]
    (fn []
      (when (instance? js/Error @resources)
        (dispatch [::messages-events/add
                   (let [{:keys [status message]} (response/parse-ex-info @resources)]
                     {:header  (cond-> (@tr [:error])
                                       status (str " (" status ")"))
                      :message message
                      :type    :error})]))
      [:div
       "HELLO"
       #_[resource-add-form]
       #_[ui/Menu {:attached   "top"
                 :borderless true}
        [search-button]
        [select-fields]
        (when (can-add? (-> @resources :operations))
          [create-button])
        [filter-button]]
       #_[ui/Segment {:attached "bottom"}
        [search-header]]])))


(defn data-resource
  []
  (let [path (subscribe [::main-subs/nav-path])
        query-params (subscribe [::main-subs/nav-query-params])]
    (fn []
      (let [[_ resource-type resource-id] @path]
        (dispatch [::data-events/set-collection-name resource-type])
        (when @query-params
          (dispatch [::data-events/set-query-params @query-params])))
      (let [n (count @path)
            children (case n
                       1 [[menu-bar]
                          [results-display]]
                       2 [[data-detail-views/detail]]
                       [[menu-bar]
                        [results-display]])]
        (vec (concat [:div] children))))))


(defmethod panel/render :data
  [path]
  [data-resource])
