(ns sixsq.slipstream.webui.application.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [sixsq.slipstream.client.api.modules :as modules]))


(def ^:const metadata-fields #{:shortName :description :category :creation :version :logoLink})


(def ^:const item-keys #{:name :description :category :version})


(defn get-module-items
  [module]
  (let [items (->> module vals first :children :item)]
    (if (map? items)
      [(select-keys items item-keys)]                       ;; single item
      (mapv #(select-keys % item-keys) items))))            ;; multiple items


(reg-fx
  ::get-module
  (fn [[client module-id]]
    (go
      (let [module (if (nil? module-id) {} (<! (modules/get-module client module-id)))

            metadata (-> module vals first (select-keys metadata-fields))

            targets (->> (-> module vals first :targets :target)
                         (map (juxt #(-> % :name keyword) :content))
                         (filter second)
                         (into {}))

            output-parameters (->> (-> module vals first :parameters :entry)
                                   (map :parameter)
                                   (filter #(= "Output" (:category %))))

            input-parameters (->> (-> module vals first :parameters :entry)
                                  (map :parameter)
                                  (filter #(= "Input" (:category %))))

            children (if (nil? module-id)
                       (<! (modules/get-module-children client nil))
                       (get-module-items module))

            module-data {:metadata         metadata
                         :targets          targets
                         :parameters       (concat input-parameters output-parameters)
                         :children         children}]
        (dispatch [:sixsq.slipstream.webui.application.events/set-module module-id module-data])))))
