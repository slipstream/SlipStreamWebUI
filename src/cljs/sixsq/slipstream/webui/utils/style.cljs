(ns sixsq.slipstream.webui.utils.style)

;;
;; common styles to be applied to Segments
;;


(def ^{:doc "Options for a Segment with no padding or special visual style.
             Can be used in place of a raw [:div].
             "}
basic
  {:basic  true
   :padded false
   :style  {:padding 0}})


(def ^{:doc "Options for a Segment with evenly spaced content."}
evenly
  (merge basic
         {:style {:display         "flex"
                  :justify-content "space-evenly"}}))


;;
;; common styles for Tables
;;

(def ^{:doc "Style for very compact, single-line definition table."}
definition
  {:compact     "very"
   :definition  true
   :single-line true
   :padded      false
   :style       {:max-width "100%"}})
