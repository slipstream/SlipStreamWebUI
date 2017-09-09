(ns sixsq.slipstream.webui.widget.accordeon-menu.styles
  (:require [garden.def :refer [defstyles]]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px]]))

(def header-height "50px")
(def main-theme-color "#36454f")

(defstyles main-menu
  [:#main-menu {}])

(defstyles accordeon-menu
  [:.accordeon-menu {:width "200px"}]

  [:.accordeon-menu-background {:position         "fixed"
                                :width            "100%"
                                :height           "100%"
                                :background-color "beige"
                                :opacity          0.5
                                :z-index          1}]

  [:.accordeon-header {:background-color main-theme-color
                       :padding "10px"
                       :height header-height}]

  [:.accordeon-menu-header {:background-color "darkgray"
                            :padding-left "10px"
                            :padding-right "5px"
                            :padding-top "4px"}]

  [:.accordeon-menu-sub {:background-color "Gainsboro"
                         :padding-left "20px"
                         :padding-top "10px"}]

  [:.accordeon-menu-header-selected {:background-color "gray"}]

  [:.accordeon-menu-sub-selected {:font-weight "bold"}]

  [:.accordeon-menu-sub [:a {:text-decoration "none"}]]

  [:.accordeon-menu-ctrl [:i {:color "white"}]]

  (at-media {:screen :only :min-device-width (px 375) :max-device-width (px 800)}
    [:.accordeon-menu-container {:position "fixed"}]
    [:.accordeon-menu-background {:display "none"}])

  (at-media {:screen :only :min-device-width (px 800)}
    [:.accordeon-menu-container {:position "block"
                                 :height "100%"}]
    [:.accordeon-menu-background {:display "none"}]
    [:.accordeon-menu-ctrl {:display "none"}]))

(defstyles css
  [main-menu]
  [accordeon-menu])
