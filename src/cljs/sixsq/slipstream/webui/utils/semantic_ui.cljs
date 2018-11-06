(ns sixsq.slipstream.webui.utils.semantic-ui
  "Mapping of names of Semantic UI components to the Soda Ash wrappers. This
   namespace has no real functionality; it just keeps Cursive from complaining
   about undefined symbols."
  (:require                                                 ;[soda-ash.core :as sa]
    ["semantic-ui-react" :as semantic]
    [reagent.core :as reagent]
    ["react-datepicker" :as date-picker]
    [taoensso.timbre :as log]))


;;(def Accordion (adapt-component semantic/Accordion))
;;(def AccordionTitle (adapt-component semantic/AccordionTitle))
;;(def AccordionContent (adapt-component semantic/AccordionContent))

(def Breadcrumb (reagent/adapt-react-class semantic/Breadcrumb))
(def BreadcrumbDivider (reagent/adapt-react-class semantic/BreadcrumbDivider))
(def BreadcrumbSection (reagent/adapt-react-class semantic/BreadcrumbSection))

(def Button (reagent/adapt-react-class semantic/Button))
(def ButtonGroup (reagent/adapt-react-class semantic/ButtonGroup))

(def Card (reagent/adapt-react-class semantic/Card))
(def CardContent (reagent/adapt-react-class semantic/CardContent))
(def CardDescription (reagent/adapt-react-class semantic/CardDescription))
(def CardGroup (reagent/adapt-react-class semantic/CardGroup))
(def CardHeader (reagent/adapt-react-class semantic/CardHeader))
(def CardMeta (reagent/adapt-react-class semantic/CardMeta))

(def Checkbox (reagent/adapt-react-class semantic/Checkbox))

(def Confirm (reagent/adapt-react-class semantic/Confirm))

(def Container (reagent/adapt-react-class semantic/Container))

(def DatePicker (reagent/adapt-react-class date-picker/default))

(def Dimmer (reagent/adapt-react-class semantic/Dimmer))
(def DimmerDimmable (reagent/adapt-react-class semantic/DimmerDimmable))

(def Divider (reagent/adapt-react-class semantic/Divider))

(def Dropdown (reagent/adapt-react-class semantic/Dropdown))
(def DropdownDivider (reagent/adapt-react-class semantic/DropdownDivider))
(def DropdownItem (reagent/adapt-react-class semantic/DropdownItem))
(def DropdownMenu (reagent/adapt-react-class semantic/DropdownMenu))

;;(def Feed (adapt-component semantic/Feed))
;;(def FeedContent (adapt-component semantic/FeedContent))
;;(def FeedDate (adapt-component semantic/FeedDate))
;;(def FeedEvent (adapt-component semantic/FeedEvent))
;;(def FeedExtra (adapt-component semantic/FeedExtra))
;;(def FeedLabel (adapt-component semantic/FeedLabel))
;;(def FeedLike (adapt-component semantic/FeedLike))
;;(def FeedMeta (adapt-component semantic/FeedMeta))
;;(def FeedSummary (adapt-component semantic/FeedSummary))
;;(def FeedUser (adapt-component semantic/FeedUser))

(def Form (reagent/adapt-react-class semantic/Form))
;;(def FormButton (adapt-component semantic/FormButton))
(def FormDropdown (reagent/adapt-react-class semantic/FormDropdown))
(def FormField (reagent/adapt-react-class semantic/FormField))
(def FormGroup (reagent/adapt-react-class semantic/FormGroup))
(def FormInput (reagent/adapt-react-class semantic/FormInput))
(def FormSelect (reagent/adapt-react-class semantic/FormSelect))

(def Grid (reagent/adapt-react-class semantic/Grid))
;;(def GridColumn (adapt-component semantic/GridColumn))
;;(def GridRow (adapt-component semantic/GridRow))

(def Icon (reagent/adapt-react-class semantic/Icon))
(def IconGroup (reagent/adapt-react-class semantic/IconGroup))

;;(def Item (adapt-component semantic/Item))
;;(def ItemContent (adapt-component semantic/ItemContent))
;;(def ItemDescription (adapt-component semantic/ItemDescription))
;;(def ItemExtra (adapt-component semantic/ItemExtra))
;;(def ItemGroup (adapt-component semantic/ItemGroup))
;;(def ItemHeader (adapt-component semantic/ItemHeader))
;;(def ItemImage (adapt-component semantic/ItemImage))
;;(def ItemMeta (adapt-component semantic/ItemMeta))

(def Image (reagent/adapt-react-class semantic/Image))

(def Input (reagent/adapt-react-class semantic/Input))

(def Header (reagent/adapt-react-class semantic/Header))
;;(def HeaderContent (adapt-component semantic/HeaderContent))
(def HeaderSubheader (reagent/adapt-react-class semantic/HeaderSubheader))

(def Label (reagent/adapt-react-class semantic/Label))
(def LabelDetail (reagent/adapt-react-class semantic/LabelDetail))

(def ListSA (reagent/adapt-react-class semantic/List))
(def ListContent (reagent/adapt-react-class semantic/ListContent))
(def ListDescription (reagent/adapt-react-class semantic/ListDescription))
(def ListHeader (reagent/adapt-react-class semantic/ListHeader))
(def ListIcon (reagent/adapt-react-class semantic/ListIcon))
(def ListItem (reagent/adapt-react-class semantic/ListItem))

(def Loader (reagent/adapt-react-class semantic/Loader))

(def MenuRaw semantic/Menu)

(def Menu (reagent/adapt-react-class semantic/Menu))
(def MenuItem (reagent/adapt-react-class semantic/MenuItem))
(def MenuMenu (reagent/adapt-react-class semantic/MenuMenu))

(def Message (reagent/adapt-react-class semantic/Message))
(def MessageHeader (reagent/adapt-react-class semantic/MessageHeader))
(def MessageContent (reagent/adapt-react-class semantic/MessageContent))
;;(def MessageList (adapt-component semantic/MessageList))
;;(def MessageItem (adapt-component semantic/MessageItem))

(def Modal (reagent/adapt-react-class semantic/Modal))
(def ModalActions (reagent/adapt-react-class semantic/ModalActions))
(def ModalContent (reagent/adapt-react-class semantic/ModalContent))
(def ModalHeader (reagent/adapt-react-class semantic/ModalHeader))

(def Pagination (reagent/adapt-react-class semantic/Pagination))

(def Popup (reagent/adapt-react-class semantic/Popup))
(def PopupHeader (reagent/adapt-react-class semantic/PopupHeader))
(def PopupContent (reagent/adapt-react-class semantic/PopupContent))
(def Progress (reagent/adapt-react-class semantic/Progress))

;;(def Rail (adapt-component semantic/Rail))
;;(def Ref (adapt-component semantic/Ref))

(def Responsive (reagent/adapt-react-class semantic/Responsive))

(def SegmentRaw semantic/Segment)
(def Segment (reagent/adapt-react-class semantic/Segment))
;;(def SegmentGroup (adapt-component semantic/SegmentGroup))

(def Sidebar (reagent/adapt-react-class semantic/Sidebar))
(def SidebarPushable (reagent/adapt-react-class semantic/SidebarPushable))
(def SidebarPusher (reagent/adapt-react-class semantic/SidebarPusher))

(def Statistic (reagent/adapt-react-class semantic/Statistic))
(def StatisticGroup (reagent/adapt-react-class semantic/StatisticGroup))
(def StatisticLabel (reagent/adapt-react-class semantic/StatisticLabel))
(def StatisticValue (reagent/adapt-react-class semantic/StatisticValue))

(def Tab (reagent/adapt-react-class semantic/Tab))
(def TabPane (reagent/adapt-react-class semantic/TabPane))

(def Table (reagent/adapt-react-class semantic/Table))
(def TableBody (reagent/adapt-react-class semantic/TableBody))
(def TableCell (reagent/adapt-react-class semantic/TableCell))
(def TableFooter (reagent/adapt-react-class semantic/TableFooter))
(def TableHeader (reagent/adapt-react-class semantic/TableHeader))
(def TableHeaderCell (reagent/adapt-react-class semantic/TableHeaderCell))
(def TableRow (reagent/adapt-react-class semantic/TableRow))

(def TextArea (reagent/adapt-react-class semantic/TextArea))

(def Transition (reagent/adapt-react-class semantic/Transition))

(def TransitionablePortal (reagent/adapt-react-class semantic/TransitionablePortal))
