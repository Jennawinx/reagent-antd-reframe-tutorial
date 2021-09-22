(ns reagent-frontend-app.l2
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
   [syn-antd.modal :as modal]))



;; -------------------------
;; Utils

(defn element-value
  "Gets the value of the targeted element"
  [e]
  (-> e .-target .-value))

;; -------------------------
;; Views

(def state
  (r/atom {:tags  [["bug" "red"] ["feature" "geekblue"]]
           #_#_:lanes ["Todo" "Todo Today"]
           #_#_:cards {"Todo"       []
                   "Todo Today" []}}))

(defn show-tags []
  [:div
   [:h3 "Tags"]
   (for [[tag-name color] (get @state :tags)]
     ^{:key tag-name}
     [tag/tag {:color color} tag-name])])

(defn add-tag []
  (let [tag-name     (r/atom "")
        tag-color    (r/atom "blue")
        on-submit-fn (fn [_]
                       (swap! state update-in [:tags] conj [@tag-name @tag-color]))]
    (fn []
      [form/form
       {:on-finish #()}
       [form/form-item
        {:label "Tag"
        ;;  :required true
         ;; Rules don't work
         #_#_:rules [{:required true :message "Please provide a name first!"}]}
        [input/input
         {:value     @tag-name
          :on-change (fn [element]
                       (reset! tag-name (element-value element)))}]]
       [form/form-item
        {:label "Color"}
        [input/input
         {:value     @tag-color
          :on-change (fn [element]
                       (reset! tag-color (element-value element)))}]]
       [form/form-item
        [button/button
         {:type :primary
          :htmlType :submit
          :on-click on-submit-fn}
         "submit"]]])))

(defn tag-editor []
  [:div
   [add-tag]
   [show-tags]])

(defn home-page []
  [:div
   [:h2 "Tag Editor"]
   [tag-editor]])



;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))