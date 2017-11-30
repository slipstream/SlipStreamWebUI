(ns vms.tables-utils
  (:require [reagent.core :as reagent :refer [atom]]
            [soda-ash.core :as sa]))

(enable-console-print!)

(defn table-navigator-footer [records-count-fn page-count-fn current-page-fn headers-count-fn
                              next-page-fn back-page-fn first-page-fn last-page-fn select-page-fn]
  (let [first-button (atom 1)]
    (fn []
      (let [number-button-nav 5
            records-count (records-count-fn)
            page-count (page-count-fn)
            current-page (current-page-fn)
            headers-count (headers-count-fn)
            button-range (take number-button-nav (range @first-button (inc page-count)))]
        [sa/TableFooter
         [sa/TableRow
          [sa/TableHeaderCell {:col-span (str headers-count)}
           [sa/Label "Found" [sa/LabelDetail records-count]]
           [sa/Menu {:floated "right" :size "mini"}
            [sa/MenuItem {:link    true :disabled (= current-page 1)
                          :onClick (fn [e d]
                                     (reset! first-button 1)
                                     (first-page-fn e d))}
             [sa/Icon {:name "angle double left"}]]
            [sa/MenuItem {:link    true :disabled (= current-page 1)
                          :onClick (fn [e d]
                                     (when (> current-page 1)
                                       (when (> @first-button 1) (swap! first-button dec))
                                       (back-page-fn e d)
                                       ))}
             [sa/Icon {:name "angle left"}]]
            (doall
              (for [i button-range]
                ^{:key i} [sa/MenuItem {:link    true
                                        :active  (= current-page i)
                                        :onClick (fn [e d]
                                                   (select-page-fn e d))} i]))
            [sa/MenuItem {:link    true :disabled (>= current-page page-count)
                          :onClick (fn [e d] (when (< current-page page-count)
                                               (when (< @first-button (- page-count number-button-nav)) (swap! first-button inc))
                                               (next-page-fn e d)))}
             [sa/Icon {:name "angle right"}]]
            [sa/MenuItem {:link    true :disabled (>= current-page page-count)
                          :onClick (fn [e d]
                                     (reset! first-button
                                             (let [first-should-be (- page-count (dec number-button-nav))]
                                               (if (neg-int? first-should-be) 1 first-should-be)))
                                     (last-page-fn e d))}
             [sa/Icon {:name "angle double right"}]]
            ]]]
         ]))))
