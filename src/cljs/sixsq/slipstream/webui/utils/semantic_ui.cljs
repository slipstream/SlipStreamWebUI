(ns sixsq.slipstream.webui.utils.semantic-ui
  "Mapping of names of Semantic UI components to the Soda Ash wrappers. This
   namespace has no real functionality; it just keeps Cursive from complaining
   about undefined symbols."
  (:require
    [cljsjs.react-datepicker]
    cljsjs.semantic-ui-react
    [reagent.core :as reagent]))

(defn array-get-semantic
  [tag]
  (aget js/semanticUIReact tag))

(defn adapt-semantic-component [tag]
  (reagent/adapt-react-class
    (array-get-semantic tag)))

;;(def Accordion (adapt-semantic-component "Accordion"))
;;(def AccordionTitle (adapt-semantic-component "AccordionTitle"))
;;(def AccordionContent (adapt-semantic-component "AccordionContent"))

(def Breadcrumb (adapt-semantic-component "Breadcrumb"))
(def BreadcrumbDivider (adapt-semantic-component "BreadcrumbDivider"))
(def BreadcrumbSection (adapt-semantic-component "BreadcrumbSection"))

(def Button (adapt-semantic-component "Button"))
(def ButtonGroup (adapt-semantic-component "ButtonGroup"))

(def Card (adapt-semantic-component "Card"))
(def CardContent (adapt-semantic-component "CardContent"))
(def CardDescription (adapt-semantic-component "CardDescription"))
(def CardGroup (adapt-semantic-component "CardGroup"))
(def CardHeader (adapt-semantic-component "CardHeader"))
(def CardMeta (adapt-semantic-component "CardMeta"))

(def Checkbox (adapt-semantic-component "Checkbox"))

(def Confirm (adapt-semantic-component "Confirm"))

(def Container (adapt-semantic-component "Container"))

(def DatePicker (reagent/adapt-react-class js/DatePicker))

(def Dimmer (adapt-semantic-component "Dimmer"))
(def DimmerDimmable (adapt-semantic-component "DimmerDimmable"))

(def Divider (adapt-semantic-component "Divider"))

(def Dropdown (adapt-semantic-component "Dropdown"))
(def DropdownDivider (adapt-semantic-component "DropdownDivider"))
(def DropdownItem (adapt-semantic-component "DropdownItem"))
(def DropdownMenu (adapt-semantic-component "DropdownMenu"))

;;(def Feed (adapt-semantic-component "Feed"))
;;(def FeedContent (adapt-semantic-component "FeedContent"))
;;(def FeedDate (adapt-semantic-component "FeedDate"))
;;(def FeedEvent (adapt-semantic-component "FeedEvent"))
;;(def FeedExtra (adapt-semantic-component "FeedExtra"))
;;(def FeedLabel (adapt-semantic-component "FeedLabel"))
;;(def FeedLike (adapt-semantic-component "FeedLike"))
;;(def FeedMeta (adapt-semantic-component "FeedMeta"))
;;(def FeedSummary (adapt-semantic-component "FeedSummary"))
;;(def FeedUser (adapt-semantic-component "FeedUser"))

(def Form (adapt-semantic-component "Form"))
;;(def FormButton (adapt-semantic-component "FormButton"))
(def FormDropdown (adapt-semantic-component "FormDropdown"))
(def FormField (adapt-semantic-component "FormField"))
(def FormGroup (adapt-semantic-component "FormGroup"))
(def FormInput (adapt-semantic-component "FormInput"))
(def FormSelect (adapt-semantic-component "FormSelect"))

(def Grid (adapt-semantic-component "Grid"))
(def GridColumn (adapt-semantic-component "GridColumn"))
;;(def GridRow (adapt-semantic-component "GridRow"))

(def Icon (adapt-semantic-component "Icon"))
(def IconGroup (adapt-semantic-component "IconGroup"))

;;(def Item (adapt-semantic-component "Item"))
;;(def ItemContent (adapt-semantic-component "ItemContent"))
;;(def ItemDescription (adapt-semantic-component "ItemDescription"))
;;(def ItemExtra (adapt-semantic-component "ItemExtra"))
;;(def ItemGroup (adapt-semantic-component "ItemGroup"))
;;(def ItemHeader (adapt-semantic-component "ItemHeader"))
;;(def ItemImage (adapt-semantic-component "ItemImage"))
;;(def ItemMeta (adapt-semantic-component "ItemMeta"))

(def Image (adapt-semantic-component "Image"))

(def Input (adapt-semantic-component "Input"))

(def Header (adapt-semantic-component "Header"))
;;(def HeaderContent (adapt-semantic-component "HeaderContent"))
(def HeaderSubheader (adapt-semantic-component "HeaderSubheader"))

(def Label (adapt-semantic-component "Label"))
(def LabelDetail (adapt-semantic-component "LabelDetail"))

(def ListSA (adapt-semantic-component "List"))
(def ListContent (adapt-semantic-component "ListContent"))
(def ListDescription (adapt-semantic-component "ListDescription"))
(def ListHeader (adapt-semantic-component "ListHeader"))
(def ListIcon (adapt-semantic-component "ListIcon"))
(def ListItem (adapt-semantic-component "ListItem"))

(def Loader (adapt-semantic-component "Loader"))

(def Menu (adapt-semantic-component "Menu"))
(def MenuItem (adapt-semantic-component "MenuItem"))
(def MenuMenu (adapt-semantic-component "MenuMenu"))

(def Message (adapt-semantic-component "Message"))
(def MessageHeader (adapt-semantic-component "MessageHeader"))
(def MessageContent (adapt-semantic-component "MessageContent"))
;;(def MessageList (adapt-semantic-component "MessageList"))
;;(def MessageItem (adapt-semantic-component "MessageItem"))

(def Modal (adapt-semantic-component "Modal"))
(def ModalActions (adapt-semantic-component "ModalActions"))
(def ModalContent (adapt-semantic-component "ModalContent"))
(def ModalHeader (adapt-semantic-component "ModalHeader"))

(def Pagination (adapt-semantic-component "Pagination"))

(def Popup (adapt-semantic-component "Popup"))
(def PopupHeader (adapt-semantic-component "PopupHeader"))
(def PopupContent (adapt-semantic-component "PopupContent"))
(def Progress (adapt-semantic-component "Progress"))

;;(def Rail (adapt-semantic-component "Rail"))
;;(def Ref (adapt-semantic-component "Ref"))

(def Responsive (adapt-semantic-component "Responsive"))

(def Segment (adapt-semantic-component "Segment"))
;;(def SegmentGroup (adapt-semantic-component "SegmentGroup"))

(def Sidebar (adapt-semantic-component "Sidebar"))
(def SidebarPushable (adapt-semantic-component "SidebarPushable"))
(def SidebarPusher (adapt-semantic-component "SidebarPusher"))

(def Statistic (adapt-semantic-component "Statistic"))
(def StatisticGroup (adapt-semantic-component "StatisticGroup"))
(def StatisticLabel (adapt-semantic-component "StatisticLabel"))
(def StatisticValue (adapt-semantic-component "StatisticValue"))

(def Step (adapt-semantic-component "Step"))
(def StepGroup (adapt-semantic-component "StepGroup"))
(def StepContent (adapt-semantic-component "StepContent"))
(def StepDescription (adapt-semantic-component "StepDescription"))
(def StepTitle (adapt-semantic-component "StepTitle"))

(def Tab (adapt-semantic-component "Tab"))
(def TabPane (adapt-semantic-component "TabPane"))

(def Table (adapt-semantic-component "Table"))
(def TableBody (adapt-semantic-component "TableBody"))
(def TableCell (adapt-semantic-component "TableCell"))
(def TableFooter (adapt-semantic-component "TableFooter"))
(def TableHeader (adapt-semantic-component "TableHeader"))
(def TableHeaderCell (adapt-semantic-component "TableHeaderCell"))
(def TableRow (adapt-semantic-component "TableRow"))

(def TextArea (adapt-semantic-component "TextArea"))

(def Transition (adapt-semantic-component "Transition"))

(def TransitionablePortal (adapt-semantic-component "TransitionablePortal"))
