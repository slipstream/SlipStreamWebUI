(ns sixsq.slipstream.webui.i18n.dictionary
  ;; the moment.js locale must be included for any supported language
  (:require
    ["moment/locale/fr"]))


(def dictionary
  {:en {:lang                     "english"

        :about                    "about"
        :about-subtitle           "This software is brought to you"
        :access-service           "access service"
        :active?                  "active only?"
        :actions                  "actions"
        :add                      "add"
        :aggregation              "aggregation"
        :all-credentials          "all credentials"
        :all-users                "all users"
        :already-registered?      "Already registered?"
        :and                      "and"
        :api-doc                  "API documentation"
        :application              "application"
        :appstore                 "app store"
        :are-you-sure?            "Are you sure?"
        :attributes               "attributes"
        :billable-only?           "billable only?"
        :cancel                   "cancel"
        :cimi                     "cimi"
        :clear                    "clear"
        :clear-all                "clear all"
        :click-for-depl-details   "click on the card for deployment details"
        :close                    "close"
        :cloud                    "cloud"
        :columns                  "columns"
        :configure                "configure"
        :core-license             "Core license"
        :count                    "count"
        :credentials              "credentials"
        :current-user             "current user"
        :create                   "create"
        :created                  "created"
        :custom                   "custom"
        :dashboard                "dashboard"
        :data                     "data"
        :delete                   "delete"
        :delete-resource          "delete resource"
        :delete-resource-msg      "delete resource %1?"
        :delta-min                "delta [min]"
        :deployment               "deployment"
        :describe                 "describe"
        :description              "description"
        :details                  "details"
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
        :forgot-password          "Forgot your password?"
        :from                     "from"
        :global-parameters        "global parameters"
        :id                       "id"
        :image                    "image"
        :input-parameters         "input parameters"
        :job                      "job"
        :knowledge-base           "knowledge base"
        :last                     "last"
        :last-30-days             "last 30 days"
        :last-7-days              "last 7 days"
        :launch                   "launch"
        :legacy-application       "legacy application"
        :less                     "less"
        :limit                    "limit"
        :loading                  "loading"
        :login                    "login"
        :login-failed             "login failed"
        :login-link               "Login."
        :logout                   "logout"
        :message                  "message"
        :messages                 "messages"
        :metrics                  "metrics"
        :module                   "module"
        :modules                  "modules"
        :more                     "more"
        :more-info                "More information..."
        :name                     "name"
        :next-step                "next step"
        :no                       "no"
        :no-account?              "No account?"
        :no-apps                  "no matching applications"
        :no-credentials           "no credentials for selected infrastructure"
        :no-data                  "no data"
        :no-data-location         "no location with selected data"
        :no-datasets              "no datasets"
        :no-input-parameters      "no input parameters defined for the application"
        :no-messages              "no messages"
        :notifications            "notifications"
        :nuvlabox                 "NuvlaBox"
        :nuvlabox-ctrl            "edge control"
        :object-count             "Number of data objects: %1"
        :objects                  "objects"
        :offset                   "offset"
        :order                    "order"
        :output-parameters        "output parameters"
        :parameters               "parameters"
        :personae-desc            "Personae description"
        :previous-step            "previous step"
        :process                  "process"
        :profile                  "profile"
        :progress                 "progress"
        :project                  "project"
        :quota                    "quota"
        :refresh                  "refresh"
        :release-notes            "release notes"
        :reports                  "reports"
        :reset-password           "reset password"
        :reset-password-inst      "Enter your username to reset your password. We'll send an email with instructions."
        :reset-password-error     "Error resetting password"
        :reset-password-success   "Success"
        :resource-type            "resource type"
        :results                  "results"
        :return-code              "return code"
        :search                   "search"
        :select                   "select"
        :select-application       "select application"
        :select-datasets          "click on cards to select or deselect dataset(s)"
        :select-file              "select file"
        :session                  "current session"
        :session-expires          "session expires"
        :settings                 "settings"
        :signup                   "sign up"
        :signup-failed            "sign up failed"
        :signup-link              "Sign up."
        :size                     "size"
        :source-code-on           "source code on"
        :start                    "start"
        :state                    "state"
        :statistics               "statistics"
        :status                   "status"
        :stop                     "stop"
        :summary                  "summary"
        :support                  "support"
        :tags                     "tags"
        :tech-doc                 "technical documentation"
        :terminate                "terminate"
        :timestamp                "timestamp"
        :to                       "to"
        :today                    "today"
        :total                    "total"
        :tutorials                "tutorials"
        :type                     "type"
        :unauthorized             "unauthorized"
        :update                   "update"
        :usage                    "usage"
        :usage-filter             "filter by users or roles - e.g. SixSq:can_deploy"
        :url                      "URL"
        :username                 "username"
        :version-number           "Version number"
        :value                    "value"
        :vms                      "VMs"
        :welcome                  "welcome"
        :welcome-cimi-desc        "Browse the CIMI resources on the server."
        :welcome-detail           "Complete solution to manage your multi-cloud to edge continuum"
        :welcome-dashboard-desc   "One glance to understand at once all that matters"
        :welcome-data-desc        "View and process datasets"
        :welcome-docs-desc        "Learn about the CIMI resources using the API documentation."
        :welcome-quota-desc       "View and control consumption on all credentials"
        :welcome-usage-desc       "Understand past and historical usage"
        :welcome-appstore-desc    "Browse apps, components and images published and shared"
        :welcome-deployment-desc  "See and control all your apps, across all clouds and edge"
        :welcome-application-desc "Manage apps, components and images. Collaborate, share and explore one-click deployable applications"
        :welcome-nuvlabox-desc    "Add IoT and edge management. Control all your NuvlaBoxes from one place"
        :welcome-metrics-desc     "View a wealth of metrics giving you real insights on the health of the system"
        :yes                      "yes"
        :yesterday                "yesterday"}

   :fr {:lang                   "français"

        :add                    "ajouter"
        :access-service         "accéder service"
        :actions                "actions"
        :active?                "uniquement actif ?"
        :aggregation            "aggréger"
        :all-users              "tous les utilisateurs"
        :all-credentials        "toutes les informations d'identification"
        :already-registered?    "Déjà enregistré ?"
        :application            "application"
        :are-you-sure?          "Êtes-vous sûr ?"
        :attributes             "attributs"
        :billable-only?         "facturable seulement ?"
        :cancel                 "annuler"
        :close                  "fermer"
        :cloud                  "nuage"
        :cimi                   "cimi"
        :clear                  "effacer"
        :clear-all              "tout effacer"
        :click-for-depl-details "cliquez sur une carte pour afficher le détail du déploiement"
        :columns                "colonnes"
        :configure              "configurer"
        :count                  "décompte"
        :credentials            "informations d'identification"
        :current-user           "utilisateur actuel"
        :create                 "créer"
        :created                "créé"
        :custom                 "personnalisé"
        :dashboard              "tableau de bord"
        :data                   "données"
        :delete                 "supprimer"
        :delete-resource        "supprimer ressource"
        :delete-resource-msg    "supprimer ressource %1 ?"
        :delta-min              "delta [min]"
        :deployment             "déploiement"
        :describe               "décrire"
        :description            "description"
        :details                "détails"
        :documentation          "documentation"
        :download               "télécharger"
        :drop-file              "déposer un fichier"
        :edit                   "modifier"
        :editing                "modification en cours"
        :error                  "erreur"
        :event                  "événement"
        :events                 "événements"
        :execute-action         "exécuter la tâche"
        :execute-action-msg     "exécuter la tâche %1 ?"
        :fields                 "champs"
        :filter                 "filtre"
        :first                  "début"
        :forgot-password        "Mot de passe oublié?"
        :from                   "de"
        :global-parameters      "paramètres globaux"
        :id                     "id"
        :image                  "image"
        :input-parameters       "paramètres d'entrée"
        :job                    "tâche"
        :knowledge-base         "base de connaissance"
        :last                   "fin"
        :last-30-days           "derniers 30 jours"
        :last-7-days            "derniers 7 jours"
        :launch                 "lancer"
        :legacy-application     "anciennes applications"
        :less                   "moins"
        :limit                  "limite"
        :loading                "chargement en cours"
        :login                  "se connecter"
        :login-failed           "la connexion a échoué"
        :login-link             "Se connecter."
        :logout                 "déconnexion"
        :message                "message"
        :messages               "messages"
        :metrics                "métriques"
        :module                 "module"
        :modules                "modules"
        :more                   "plus"
        :more-info              "Plus d'informations"
        :name                   "nom"
        :next-step              "étape suivante"
        :no                     "non"
        :no-account?            "Pas de compte ?"
        :no-apps                "pas d'applications correspondantes"
        :no-credentials         "pas d'informations d'identifcation pour l'infrastructure sélectionné"
        :no-data                "pas de données"
        :no-data-location       "aucun lieu contien les données sélectionnées"
        :no-datasets            "pas de collection de données"
        :no-input-parameters    "aucun paramètre d'entrée définié pour l'application"
        :no-messages            "aucun message"
        :notifications          "notifications"
        :nuvlabox               "NuvlaBox"
        :nuvlabox-ctrl          "contrôle de bord"
        :object-count           "Nombre d'objets de données : %1"
        :objects                "objets"
        :offset                 "décalage"
        :order                  "ordonner"
        :output-parameters      "paramètres de sortie"
        :parameters             "paramètres"
        :previous-step          "étape précédente"
        :process                "traiter"
        :profile                "profil utilisateur"
        :progress               "progression"
        :project                "projet"
        :quota                  "quota"
        :refresh                "actualiser"
        :reports                "rapports"
        :reset-password         "réinitialiser le mot de passe"
        :reset-password-inst    "Entrez votre nom d'utilisateur pour réinitialiser votre mot de passe. Nous vous enverrons un email avec les instructions."
        :reset-password-error   "Erreur lors de la réinitialisation du mot de passe"
        :reset-password-success "Succès"
        :resource-type          "type de ressource"
        :results                "résultats"
        :return-code            "code de retour"
        :search                 "chercher"
        :select                 "selection"
        :select-application     "selection d'application"
        :select-datasets        "cliquez sur les cartes pour selectionner ou deselectionner les collections de données"
        :select-file            "choisir un fichier"
        :session                "session actuelle"
        :session-expires        "session se termine"
        :settings               "paramètres"
        :summary                "résumé"
        :support                "support"
        :signup                 "s'inscrire"
        :signup-failed          "l'inscription a échoué"
        :signup-link            "S'inscrire."
        :size                   "taille"
        :start                  "début"
        :state                  "état"
        :statistics             "statistiques"
        :status                 "statut"
        :stop                   "stop"
        :tags                   "mots clés"
        :terminate              "terminer"
        :timestamp              "horodatage"
        :to                     "à"
        :today                  "aujourd'hui"
        :total                  "total"
        :type                   "type"
        :unauthorized           "non autorisé"
        :update                 "mettre à jour"
        :url                    "URL"
        :usage                  "usage"
        :usage-filter           "filtrer par utilisateurs ou rôles, ex.: SixSq:can_deploy"
        :username               "nom d'utilisateur"
        :version-number         "Numéro de version"
        :value                  "valeur"
        :vms                    "VMs"
        :welcome                "bienvenue"
        :welcome-cimi-desc      "Parcourir les ressources CIMI sur le serveur"
        :welcome-data-desc      "Voir et traiter les jeux de données"
        :welcome-detail         "La solution complète pour gérer votre continuum multi-cloud et edge"
        :yes                    "oui"
        :yesterday              "hier"}})
