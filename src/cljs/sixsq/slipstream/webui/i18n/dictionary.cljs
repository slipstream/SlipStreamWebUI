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
        :add-column "add column"
        :apps "applications"
        :cimi "cimi"
        :cloud "cloud"
        :copyright "copyright"
        :dashboard "dashboard"
        :fields "fields"
        :filter "filter"
        :first "first"
        :home "home"
        :id "id"
        :last "last"
        :limit "limit"
        :login "login"
        :logout "logout"
        :module "module"
        :offer "offer"
        :offers "offers"
        :offset "offset"
        :password "password"
        :profile "%1's user profile"
        :remove-column "remove column"
        :resource-type "resource type"
        :results "results"
        :search "search"
        :show "show"
        :start "start"
        :status "status"
        :tag "tag"
        :username "username"
        :vms "VMs"}

   :fr {:lang "français"

        :active-only "active uniquement?"
        :add-column "ajouter une colonne"
        :apps "applications"
        :cimi "cimi"
        :cloud "cloud"
        :copyright "droits d'auteur"
        :dashboard "tableau de bord"
        :fields "champs"
        :filter "filtre"
        :first "premier"
        :home "chez moi"
        :id "id"
        :last "dernier"
        :limit "limite"
        :login "connexion"
        :logout "déconnexion"
        :module "module"
        :offer "offre"
        :offers "offres"
        :offset "début"
        :password "mot de passe"
        :profile "profile d'utilisateur de %1"
        :remove-column "effacer la colonne"
        :resource-type "type de resource"
        :results "resultats"
        :search "chercher"
        :show "afficher"
        :start "début"
        :status "statut"
        :tag "etiquette"
        :username "nom d'utilisateur"
        :vms "MVs"}})



