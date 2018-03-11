(ns sixsq.slipstream.webui.profile.views
  (:require
    [re-frame.core :refer [subscribe]]

    [cljs-time.core :as time]
    [cljs-time.format :as time-format]
    [cljs-time.coerce :as time-coerce]

    [sixsq.slipstream.webui.panel :as panel]

    [sixsq.slipstream.webui.authn.subs :as authn-subs]
    [sixsq.slipstream.webui.cimi-api.utils :as cimi-api-utils]
    [sixsq.slipstream.webui.i18n.subs :as i18n-subs]

    [sixsq.slipstream.webui.utils.values :as values]
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]
    [clojure.string :as str]
    [sixsq.slipstream.webui.utils.collapsible-card :as cc]))


(defn tuple-to-row [[v1 v2]]
  [ui/TableRow
   [ui/TableCell {:collapsing true} (str v1)]
   [ui/TableCell v2]])


(def data-to-tuple
  (juxt (comp name first) (comp values/format-value second)))


(defn id-as-href
  [[k v :as entry]]
  (if (= "id" k)
    [k (values/as-href {:href v})]
    entry))


(defn username-as-href
  [[k v :as entry]]
  (if (= "username" k)
    [k (values/as-href {:href (str "user/" v)})]
    entry))


(defn group-table-sui
  [group-data]
  [ui/Table {:compact     true
             :definition  true
             :single-line true
             :padded      false
             :style       {:max-width "100%"}}
   (vec (concat [ui/TableBody]
                (->> group-data
                     (map data-to-tuple)
                     (map id-as-href)
                     (map username-as-href)
                     (map tuple-to-row))))])


(def rfc822 (time-format/formatters :rfc822))


;;
;; FIXME: Should be replaced with use of moment.js.
;;
(defn minutes-remaining
  [expiry]
  (if expiry
    (let [fixed-tz (str/replace expiry #"UTC" "Z")
          date-time (time-format/parse rfc822 fixed-tz)
          expiry-ms (time-coerce/to-long date-time)
          now-ms (time-coerce/to-long (time/now))
          remaining-mins (int (/ (max (- expiry-ms now-ms) 0) 60000.))]
      (str remaining-mins " minutes"))
    "unknown"))


(def session-keys #{:id :username :roles :clientIP})


(def session-keys-order {:id 0, :username 1, :roles 2, :clientIP 3, :time-remaining 4})


(defn add-index
  [[k _ :as entry]]
  (-> k
      (session-keys-order 5)
      (cons entry)))


(defn process-session-data
  [data]
  (let [expiry (second (first (filter #(= :expiry (first %)) data)))]
    (->> data
         (filter #(session-keys (first %)))
         (cons [:time-remaining (minutes-remaining expiry)])
         (map add-index)
         (sort-by first)
         (map rest))))


(defn session-info
  "Provides the user's session information."
  []
  (let [tr (subscribe [::i18n-subs/tr])
        session @(subscribe [::authn-subs/session])
        data (process-session-data (cimi-api-utils/remove-common-attrs session))]
    [cc/collapsible-card
     (@tr [:session])
     (if session
       (group-table-sui data)
       [:p (@tr [:no-session])])]))


(defn panel-title
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [cc/title-card (@tr [:profile])]))


(defmethod panel/render :profile
  [path]
  [ui/Container {:fluid true}
   [panel-title]
   [session-info]])

