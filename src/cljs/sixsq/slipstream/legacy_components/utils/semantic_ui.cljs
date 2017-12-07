(ns
    ^{:copyright "Copyright 2017, Charles A. Loomis, Jr."
          :license   "http://www.apache.org/licenses/LICENSE-2.0"}
    sixsq.slipstream.legacy-components.utils.semantic-ui
    "Mapping of names of Semantic UI components to the Soda Ash wrappers. This
       namespace has no real functionality; it just keeps Cursive from complaining
       about undefined symbols."
    (:require [soda-ash.core :as sa]))

(def Breadcrumb sa/Breadcrumb)
(def BreadcrumbDivider sa/BreadcrumbDivider)
(def BreadcrumbSection sa/BreadcrumbSection)

(def Button sa/Button)

(def Card sa/Card)
(def CardContent sa/CardContent)
(def CardDescription sa/CardDescription)
(def CardHeader sa/CardHeader)

(def Checkbox sa/Checkbox)

(def Container sa/Container)

(def Dimmer sa/Dimmer)
(def DimmerDimmable sa/DimmerDimmable)

(def Dropdown sa/Dropdown)
(def DropdownItem sa/DropdownItem)
(def DropdownMenu sa/DropdownMenu)

(def Form sa/Form)
(def FormField sa/FormField)
(def FormGroup sa/FormGroup)
(def FormSelect sa/FormSelect)

(def Icon sa/Icon)

(def Image sa/Image)

(def Input sa/Input)

(def Header sa/Header)

(def ListSA sa/ListSA)
(def ListContent sa/ListContent)
(def ListHeader sa/ListHeader)
(def ListItem sa/ListItem)

(def Menu sa/Menu)
(def MenuItem sa/MenuItem)
(def MenuMenu sa/MenuMenu)

(def Modal sa/Modal)
(def ModalActions sa/ModalActions)
(def ModalContent sa/ModalContent)
(def ModalHeader sa/ModalHeader)

(def Popup sa/Popup)

(def Sidebar sa/Sidebar)
(def SidebarPushable sa/SidebarPushable)
(def SidebarPusher sa/SidebarPusher)

(def Statistic sa/Statistic)
(def StatisticGroup sa/StatisticGroup)
(def StatisticLabel sa/StatisticLabel)
(def StatisticValue sa/StatisticValue)

(def Table sa/Table)
(def TableBody sa/TableBody)
(def TableCell sa/TableCell)
(def TableHeader sa/TableHeader)
(def TableHeaderCell sa/TableHeaderCell)
(def TableRow sa/TableRow)
(def TableFooter sa/TableFooter)
