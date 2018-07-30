(ns sixsq.slipstream.webui.acl.views
  (:require
    [sixsq.slipstream.webui.utils.semantic-ui :as ui]))

(defn acl-test
  []
  [ui/Table {:text-align "center", :collapsing true, :celled true, :unstackable true}
   [ui/TableHeader
    [ui/TableRow
     [ui/TableHeaderCell {:rowSpan 2, :vertical-align "bottom", :collapsing true} "principal"]
     [ui/TableHeaderCell {:rowSpan 2, :vertical-align "bottom", :collapsing true} "type"]
     [ui/TableHeaderCell {:colSpan 2, :text-align "center", :collapsing true} "metadata"]
     [ui/TableHeaderCell {:colSpan 2, :text-align "center", :collapsing true} "attributes"]
     [ui/TableHeaderCell {:colSpan 2, :text-align "center", :collapsing true} "ACL"]
     [ui/TableHeaderCell {:rowSpan 2, :vertical-align "bottom", :collapsing true} "delete"]]
    [ui/TableRow
     [ui/TableHeaderCell {:collapsing true} "view"]
     [ui/TableHeaderCell {:collapsing true} "modify"]
     [ui/TableHeaderCell {:collapsing true} "view"]
     [ui/TableHeaderCell {:collapsing true} "modify"]
     [ui/TableHeaderCell {:collapsing true} "view"]
     [ui/TableHeaderCell {:collapsing true} "modify"]]]
   [ui/TableBody
    [ui/TableRow
     [ui/TableCell {:collapsing true} "some-owner"]
     [ui/TableCell {:collapsing true} "ROLE"]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]]
    [ui/TableRow
     [ui/TableCell {:collapsing true} "someone"]
     [ui/TableCell {:collapsing true} "USER"]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} "\u0020"]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} "\u0020"]
     [ui/TableCell {:collapsing true} [ui/Icon {:name "check"}]]
     [ui/TableCell {:collapsing true} "\u0020"]
     [ui/TableCell {:collapsing true} "\u0020"]]
    ]])
