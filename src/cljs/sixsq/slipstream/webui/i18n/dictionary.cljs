(ns sixsq.slipstream.webui.i18n.dictionary
  (:require [taoensso.tempura :as tempura]))

(declare dictionary)

(defn create-tr-fn
  "Returns a translation function from the dictionary and
   the provided locale-id (which should be a string). The
   English locale (en) is always the fallback."
  [locale-id]
  (partial tempura/tr {:dict dictionary :default-locale :en} [(keyword locale-id)]))

(def dictionary
  {:en {:lang "english"

        :active-only "active only?"
        :apps "applications"
        :cimi "cimi"
        :cloud "cloud"
        :copyright "copyright"
        :dashboard "dashboard"
        :fields "fields"
        :filter "filter"
        :first "first"
        :home "home"
        :last "last"
        :limit "limit"
        :login "login"
        :logout "logout"
        :offers "offers"
        :offset "offset"
        :password "password"
        :profile "%1's user profile"
        :resource-type "resource type"
        :search "search"
        :show "show"
        :username "username"}

   :fr {:lang "français"

        :active-only "active uniquement?"
        :apps "applications"
        :cimi "cimi"
        :cloud "cloud"
        :copyright "droits d'auteur"
        :dashboard "tableau de bord"
        :fields "champs"
        :filter "filtre"
        :first "premier"
        :home "chez moi"
        :last "dernier"
        :limit "limite"
        :login "connexion"
        :logout "déconnexion"
        :offers "offres"
        :offset "début"
        :password "mot de passe"
        :profile "profile d'utilisateur de %1"
        :resource-type "type de resource"
        :search "chercher"
        :show "afficher"
        :username "nom d'utilisateur"}})



