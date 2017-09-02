(ns sixsq.slipstream.webui.components.core
  "Public API for SlipStream WebUI components."
  (:require
    [sixsq.slipstream.webui.components.breadcrumbs :as breadcrumbs]
    [sixsq.slipstream.webui.components.column :as column]
    [sixsq.slipstream.webui.components.editor :as editor]))

(def breadcrumbs breadcrumbs/breadcrumbs)

(def column column/column)

(def editor editor/editor)
(def json-editor editor/json-editor)
