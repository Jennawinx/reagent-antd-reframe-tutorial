(ns reagent-frontend-app.core
    (:require

     [reagent.core :as r]
     [reagent.dom :as d]

     [syn-antd.input :as input]
     [syn-antd.button :as button]
     [syn-antd.col :as col]
     [syn-antd.tabs :as tabs]
     [syn-antd.form :as form]
     [syn-antd.select :as select]
     [syn-antd.checkbox :as checkbox]
     [syn-antd.card :as card]
     [syn-antd.tag :as tag]
     [syn-antd.modal :as modal]

     ))

;; -------------------------
;; Utils

(defn element-value
  "Gets the value of the targeted element"
  [e]
  (-> e .-target .-value))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [:h2 "Welcome to Reagent"]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))

