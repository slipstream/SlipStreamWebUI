(ns vms.visibility)

(defn VisibleWebPage [& {:keys [onWebPageVisible onWebPageHidden] :as options}]
  (. js/document
     (addEventListener "visibilitychange"
                       #(if (.-hidden js/document)
                          (when onWebPageVisible (onWebPageVisible))
                          (when onWebPageHidden (onWebPageHidden))
                          ))))
