(ns sixsq.slipstream.webui.utils.semantic-ui
  "Mapping of names of Semantic UI components to the Soda Ash wrappers. This
   namespace has no real functionality; it just keeps Cursive from complaining
   about undefined symbols."
  (:require                                                 ;[soda-ash.core :as sa]
    [reagent.core :as r]
    cljsjs.semantic-ui-react))

(defn adapt-component [tag]
  (r/adapt-react-class (aget js/semanticUIReact tag)))

(def Accordion (adapt-component "Accordion"))
(def AccordionTitle (adapt-component "AccordionTitle"))
(def AccordionContent (adapt-component "AccordionContent"))

(def Breadcrumb (adapt-component "Breadcrumb"))
(def BreadcrumbDivider (adapt-component "BreadcrumbDivider"))
(def BreadcrumbSection (adapt-component "BreadcrumbSection"))

(def Button (adapt-component "Button"))
(def ButtonGroup (adapt-component "ButtonGroup"))

(def Card (adapt-component "Card"))
(def CardContent (adapt-component "CardContent"))
(def CardDescription (adapt-component "CardDescription"))
(def CardHeader (adapt-component "CardHeader"))
(def CardMeta (adapt-component "CardMeta"))

(def Checkbox (adapt-component "Checkbox"))

(def Confirm (adapt-component "Confirm"))

(def Container (adapt-component "Container"))

(def Dimmer (adapt-component "Dimmer"))
(def DimmerDimmable (adapt-component "DimmerDimmable"))

(def Divider (adapt-component "Divider"))

(def Dropdown (adapt-component "Dropdown"))
(def DropdownItem (adapt-component "DropdownItem"))
(def DropdownMenu (adapt-component "DropdownMenu"))

(def Form (adapt-component "Form"))
(def FormButton (adapt-component "FormButton"))
(def FormDropdown (adapt-component "FormDropdown"))
(def FormField (adapt-component "FormField"))
(def FormGroup (adapt-component "FormGroup"))
(def FormInput (adapt-component "FormInput"))
(def FormSelect (adapt-component "FormSelect"))

(def Grid (adapt-component "Grid"))
(def GridColumn (adapt-component "GridColumn"))
(def GridRow (adapt-component "GridRow"))

(def Icon (adapt-component "Icon"))
(def IconGroup (adapt-component "IconGroup"))

(def Image (adapt-component "Image"))

(def Input (adapt-component "Input"))

(def Header (adapt-component "Header"))

(def Label (adapt-component "Label"))
(def LabelDetail (adapt-component "LabelDetail"))

(def ListSA (adapt-component "List"))
(def ListContent (adapt-component "ListContent"))
(def ListDescription (adapt-component "ListDescription"))
(def ListHeader (adapt-component "ListHeader"))
(def ListIcon (adapt-component "ListIcon"))
(def ListItem (adapt-component "ListItem"))

(def Loader (adapt-component "Loader"))

(def Menu (adapt-component "Menu"))
(def MenuItem (adapt-component "MenuItem"))
(def MenuMenu (adapt-component "MenuMenu"))

(def Message (adapt-component "Message"))
(def MessageHeader (adapt-component "MessageHeader"))

(def Modal (adapt-component "Modal"))
(def ModalActions (adapt-component "ModalActions"))
(def ModalContent (adapt-component "ModalContent"))
(def ModalHeader (adapt-component "ModalHeader"))

(def Pagination (r/adapt-react-class (aget js/semanticUIReact "Pagination")))

(def Popup (adapt-component "Popup"))
(def Progress (adapt-component "Progress"))

(def Rail (adapt-component "Rail"))
(def Ref (adapt-component "Ref"))

(def Segment (adapt-component "Segment"))
(def SegmentGroup (adapt-component "SegmentGroup"))

(def Sidebar (adapt-component "Sidebar"))
(def SidebarPushable (adapt-component "SidebarPushable"))
(def SidebarPusher (adapt-component "SidebarPusher"))

(def Statistic (adapt-component "Statistic"))
(def StatisticGroup (adapt-component "StatisticGroup"))
(def StatisticLabel (adapt-component "StatisticLabel"))
(def StatisticValue (adapt-component "StatisticValue"))

(def Tab (adapt-component "Tab"))
(def TabPane (adapt-component "TabPane"))

(def Table (adapt-component "Table"))
(def TableBody (adapt-component "TableBody"))
(def TableCell (adapt-component "TableCell"))
(def TableFooter (adapt-component "TableFooter"))
(def TableHeader (adapt-component "TableHeader"))
(def TableHeaderCell (adapt-component "TableHeaderCell"))
(def TableRow (adapt-component "TableRow"))

(def TextArea (adapt-component "TextArea"))

(def Transition (adapt-component "Transition"))
