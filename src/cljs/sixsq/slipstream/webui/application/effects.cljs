(ns sixsq.slipstream.webui.application.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.modules :as modules]
    [taoensso.timbre :as log]))


(def ^:const metadata-fields #{:shortName :description :category :creation :version :logoLink})


(def ^:const item-keys #{:name :description :category :version})


(def ^:const recipe-fields #{:prerecipe :packages :recipe})


(defn get-module-items
  [module]
  (let [items (->> module vals first :children :item)]
    (if (map? items)
      [(select-keys items item-keys)]                       ;; single item
      (mapv #(select-keys % item-keys) items))))            ;; multiple items


(defn format-packages
  [{:keys [package] :as packages}]
  (let [package (if (map? package) [package] package)]
    (str/join "\n" (mapv :name package))))


(reg-fx
  ::get-module
  (fn [[client module-id]]
    (go
      (let [module (if (nil? module-id) {} (<! (modules/get-module client module-id)))

            _ (log/error (with-out-str (cljs.pprint/pprint module)))

            {:keys [prerecipe packages recipe]} (-> module vals first (select-keys recipe-fields))

            metadata (-> module vals first (select-keys metadata-fields))

            targets (cond-> (->> (-> module vals first :targets :target)
                                 (map (juxt #(-> % :name keyword) :content))
                                 (filter second)
                                 (into {}))
                            prerecipe (assoc :prerecipe prerecipe)
                            recipe (assoc :recipe recipe)
                            packages (assoc :packages (format-packages packages)))

            all-parameters (map :parameter (-> module vals first :parameters :entry))

            output-parameters (filter #(= "Output" (:category %)) all-parameters)

            input-parameters (filter #(= "Input" (:category %)) all-parameters)

            nodes (map (juxt :imageUri :name #(-> % :parameterMappings :entry)) (map :node (-> module vals first :nodes :entry)))

            _ (log/error "NODES:" (with-out-str (cljs.pprint/pprint nodes)))

            children (if (nil? module-id)
                       (<! (modules/get-module-children client nil))
                       (get-module-items module))

            module-data {:metadata   metadata
                         :targets    targets
                         :parameters (concat input-parameters output-parameters)
                         :children   children}]
        (dispatch [:sixsq.slipstream.webui.application.events/set-module module-id module-data])))))
