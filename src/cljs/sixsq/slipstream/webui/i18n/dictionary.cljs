(ns sixsq.slipstream.webui.i18n.dictionary
  ;; the moment.js locale must be included for any supported language
  (:require
    [cljsjs.moment.locale.fr]))


(def dictionary
  {:en {:lang                     "english"

        :about                    "about"
        :about-subtitle           "This software is brought to you"
        :active?                  "active only?"
        :add                      "add"
        :aggregation              "aggregation"
        :all-credentials          "all credentials"
        :all-users                "all users"
        :already-registered?      "Already registered?"
        :and                      "and"
        :api-doc                  "API documentation"
        :application              "application"
        :appstore                 "app store"
        :billable-only?           "billable only?"
        :cancel                   "cancel"
        :cimi                     "cimi"
        :clear                    "clear"
        :clear-all                "clear all"
        :close                    "close"
        :cloud                    "cloud"
        :columns                  "columns"
        :core-license             "Core license"
        :current-user             "current user"
        :create                   "create"
        :custom                   "custom"
        :dashboard                "dashboard"
        :data                     "data"
        :delete                   "delete"
        :delete-resource          "delete resource"
        :delete-resource-msg      "delete resource %1?"
        :delta-min                "delta [min]"
        :deployment               "deployment"
        :describe                 "describe"
        :documentation            "documentation"
        :download                 "download"
        :drop-file                "drop file"
        :edit                     "edit"
        :editing                  "editing"
        :error                    "error"
        :event                    "event"
        :events                   "events"
        :execute-action           "execute action"
        :execute-action-msg       "execute action %1?"
        :fields                   "fields"
        :filter                   "filter"
        :first                    "first"
        :from                     "from"
        :id                       "id"
        :input-parameters         "input parameters"
        :knowledge-base           "knowledge base"
        :last                     "last"
        :last-30-days             "last 30 days"
        :last-7-days              "last 7 days"
        :legal                    "legal information"
        :legacy-application       "legacy application"
        :less                     "less"
        :limit                    "limit"
        :loading                  "loading"
        :login                    "login"
        :login-failed             "login failed"
        :login-link               "Login."
        :logout                   "logout"
        :messages                 "messages"
        :metrics                  "metrics"
        :module                   "module"
        :modules                  "modules"
        :more                     "more"
        :more-info                "More information..."
        :no-account?              "No account?"
        :no-data                  "no data"
        :no-messages              "no messages"
        :notifications            "notifications"
        :nuvlabox                 "NuvlaBox"
        :nuvlabox-ctrl            "Edge Control"
        :offset                   "offset"
        :order                    "order"
        :output-parameters        "output parameters"
        :parameters               "parameters"
        :personae-desc            "Personae description"
        :profile                  "profile"
        :quota                    "quota"
        :refresh                  "refresh"
        :release-notes.           "release notes"
        :reports                  "reports"
        :resource-type            "resource type"
        :results                  "results"
        :search                   "search"
        :select                   "select"
        :select-file              "select file"
        :session                  "current session"
        :session-expires          "session expires"
        :settings                 "settings"
        :signup                   "sign up"
        :signup-failed            "sign up failed"
        :signup-link              "Sign up."
        :source-code-on           "source code on"
        :start                    "start"
        :state                    "state"
        :statistics               "statistics"
        :status                   "status"
        :summary                  "summary"
        :support                  "support"
        :tags                     "tags"
        :tech-doc                 "technical documentation"
        :terminate                "terminate"
        :timestamp                "timestamp"
        :to                       "to"
        :today                    "today"
        :tutorials                "tutorials"
        :type                     "type"
        :unauthorized             "unauthorized"
        :update                   "update"
        :usage                    "usage"
        :usage-filter             "filter by users or roles - e.g. SixSq:can_deploy"
        :url                      "URL"
        :username                 "username"
        :version-number           "Version number"
        :vms                      "VMs"
        :welcome                  "welcome"
        :welcome-detail           "Complete solution to manage your multi-cloud to edge continuum"
        :welcome-dashboard-desc   "One glance to understand at once all that matters"
        :welcome-quota-desc       "View and control consumption on all credentials"
        :welcome-usage-desc       "Understand paste and historical usage"
        :welcome-appstore-desc    "Browse apps, components and images published and shared"
        :welcome-deployment-desc  "See and control all your apps, across all clouds and edge"
        :welcome-application-desc "Manage apps, components and images. Collaborate, share and explore one-click deployable applications"
        :welcome-nuvlabox-desc    "Add IoT and edge management. Control all your NuvlaBoxes from one place"
        :welcome-metrics-desc     "View a wealth of metrics giving you real insights on the health of the system"
        :yesterday                "yesterday"}

   :fr {:lang                "français"

        :add                 "ajouter"
        :active?             "uniquement active ?"
        :aggregation         "aggréger"
        :all-users           "tout les utilisateurs"
        :all-credentials     "tout les informations d'identification"
        :already-registered? "Déjà enregistre ?"
        :application         "application"
        :billable-only?      "facturable seulement ?"
        :cancel              "annuler"
        :close               "fermer"
        :cloud               "nuage"
        :cimi                "cimi"
        :clear               "effacer"
        :clear-all           "tout effacer"
        :columns             "colonnes"
        :current-user        "utilisateur actuel"
        :create              "créer"
        :custom              "personnalisé"
        :dashboard           "tableau de bord"
        :data                "données"
        :delete              "supprimer"
        :delete-resource     "supprimer ressource"
        :delete-resource-msg "supprimer ressource %1?"
        :delta-min           "delta [min]"
        :deployment          "déploiement"
        :describe            "décrire"
        :documentation       "documentation"
        :download            "télécharger"
        :drop-file           "déposer un fichier"
        :edit                "modifier"
        :editing             "modification en cours"
        :error               "erreur"
        :event               "événement"
        :events              "événements"
        :execute-action      "exécuter le tâche"
        :execute-action-msg  "exécuter le tâche %1?"
        :fields              "champs"
        :filter              "filtre"
        :first               "début"
        :from                "de"
        :id                  "id"
        :input-parameters    "paramètres d'entrée"
        :knowledge-base      "knowledge base"
        :last                "fin"
        :last-30-days        "derniers 30 jours"
        :last-7-days         "derniers 7 jours"
        :legal               "mentions légales"
        :legacy-application  "anciennes applications"
        :less                "moins"
        :limit               "limite"
        :loading             "chargement en cours"
        :login               "se connecter"
        :login-failed        "la connexion a échoué"
        :login-link          "Se connecter."
        :logout              "déconnexion"
        :messages            "messages"
        :metrics             "métriques"
        :module              "module"
        :modules             "modules"
        :more                "plus"
        :more-info           "Plus d'informations"
        :no-account?         "Pas de compte ?"
        :no-data             "pas de données"
        :no-messages         "aucune message"
        :notifications       "notifications"
        :nuvlabox            "NuvlaBox"
        :offset              "décalage"
        :order               "ordonner"
        :output-parameters   "paramètres de sortie"
        :parameters          "paramètres"
        :profile             "profile d'utilisateur"
        :quota               "quota"
        :refresh             "actualiser"
        :reports             "rapports"
        :resource-type       "type de ressource"
        :results             "résultats"
        :search              "chercher"
        :select              "selection"
        :select-file         "choisir un fichier"
        :session             "session actuelle"
        :session-expires     "session termine"
        :settings            "paramètres"
        :start               "début"
        :summary             "résumé"
        :support             "support"
        :signup              "s'inscrire"
        :signup-failed       "l'inscription a échoué"
        :signup-link         "S'inscrire."
        :state               "état"
        :statistics          "statistiques"
        :status              "statut"
        :tags                "mots clés"
        :terminate           "terminer"
        :timestamp           "horodatage"
        :to                  "à"
        :today               "aujourd'hui"
        :type                "type"
        :unauthorized        "non autorisé"
        :update              "mettre à jour"
        :url                 "URL"
        :usage               "usage"
        :usage-filter        "filtrer par les utilisateurs ou les rôles, e.g. SixSq:can_deploy"
        :username            "nom d'utilisateur"
        :vms                 "VMs"
        :welcome             "bienvenue"
        :welcome-detail      "La solution complète pour manager votre continuum multi-cloud et edge"
        :yesterday           "hier"}})
