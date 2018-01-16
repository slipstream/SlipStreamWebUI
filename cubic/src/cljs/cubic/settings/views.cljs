(ns
  ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
    :license   "http://www.apache.org/licenses/LICENSE-2.0"}
  cubic.settings.views
  (:require
    [re-frame.core :refer [subscribe]]
    [cubic.panel :as panel]

    [cubic.i18n.subs :as i18n-subs]

    [cubic.utils.semantic-ui :as ui]))


(defn settings-info
  []
  (let [tr (subscribe [::i18n-subs/tr])]
    [ui/Container
     [ui/Header {:as "h1"} (@tr [:settings])]]))


(defmethod panel/render :settings
  [path query-params]
  [settings-info])
