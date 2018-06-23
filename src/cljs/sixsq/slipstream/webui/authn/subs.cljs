(ns sixsq.slipstream.webui.authn.subs
  (:require
    [clojure.string :as str]
    [re-frame.core :refer [reg-sub subscribe]]
    [sixsq.slipstream.webui.authn.spec :as authn-spec]))


(reg-sub
  ::open-modal
  ::authn-spec/open-modal)

(reg-sub
  ::session
  ::authn-spec/session)

(defn has-role? [session role]
  (some-> session :roles (str/split  #"\s+") set (contains? role)))

(reg-sub
  ::is-admin?
  :<- [::session]
  (fn [session _]
    (has-role? session "ADMIN")))

(reg-sub
  ::is-user?
  :<- [::session]
  (fn [session _]
    (has-role? session "USER")))

(reg-sub
  ::user
  :<- [::session]
  (fn [session _]
    (some-> session :username (str/replace #"user/" ""))))

(reg-sub
  ::error-message
  ::authn-spec/error-message)

(reg-sub
  ::redirect-uri
  ::authn-spec/redirect-uri)

(reg-sub
  ::server-redirect-uri
  ::authn-spec/server-redirect-uri)
