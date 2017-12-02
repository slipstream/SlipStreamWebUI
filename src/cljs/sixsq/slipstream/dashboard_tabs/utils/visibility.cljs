(ns sixsq.slipstream.dashboard-tabs.utils.visibility)

(defn VisibleWebPage [& {:keys [onWebPageVisible onWebPageHidden] :as options}]
      (. js/document
         (addEventListener "visibilitychange"
                           #(if (.-hidden js/document)
                              (when onWebPageHidden (onWebPageHidden))
                              (when onWebPageVisible (onWebPageVisible))
                              ))))
