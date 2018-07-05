(ns sixsq.slipstream.webui.authn.utils
  (:require
    [clojure.string :as str]))


(defn has-role? [session role]
  (some-> session :roles (str/split #"\s+") set (contains? role)))
