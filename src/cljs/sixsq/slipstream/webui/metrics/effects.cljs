(ns sixsq.slipstream.webui.metrics.effects
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [reg-fx dispatch]]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]))


(defn get-count
  [resp]
  (-> resp :aggregations :count:id :value))


(defn job-search
  [client params state]
  (let [filter (when state
                 (str "state = '" state "'"))
        params (assoc params :$filter filter)]
    (cimi/search client "jobs" (cimi-api-utils/sanitize-params params))))


(reg-fx
  ::fetch-job-info
  (fn [[client callback]]
    (go
      (let [params {:$aggregation "count:id", :$first 1, :$last 0}

            queued (get-count (<! (job-search client params "QUEUED")))
            success (get-count (<! (job-search client params "SUCCESS")))
            failed (get-count (<! (job-search client params "FAILED")))
            running (get-count (<! (job-search client params "RUNNING")))
            total (get-count (<! (job-search client params nil)))]

        (callback {:queued  {:category "queued", :value queued}
                   :running {:category "running", :value running}
                   :success {:category "success", :value success}
                   :failed  {:category "failed", :value failed}
                   :total   {:category "total", :value total}})))))
