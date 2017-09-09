(ns sixsq.slipstream.webui.styles
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]
            [sixsq.slipstream.webui.widget.accordeon-menu.styles :as accordeon-menu-styles]))

(defstyles base
  accordeon-menu-styles/css)
