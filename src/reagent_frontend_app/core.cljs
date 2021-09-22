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
   [syn-antd.list :as list]

   [ajax.core :as ajax]))

;; -------------------------
;; Utils

(defn element-value
  "Gets the value of the targeted element"
  [e]
  (-> e .-target .-value))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

;; -------------------------
;; Views

(def state (r/atom {:facts []}))
#_"https://catfact.ninja/facts?limit=5"

(defn fetch-facts []
  (ajax/GET "https://catfact.ninja/facts"
            {:params {:limit 5}
             :response-format :json
             :handler (fn [response] 
                        (swap! state assoc :facts (get response "data")))
             :error-handler error-handler}))

(defn facts-list []
    [:ol
     ;; Can use a for each loop here, but using index for the sake of the example
     (for [{:strs [fact length]} (get @state :facts)]
       (if (> length 100)
         ^{:key fact}
         [:li.red
          [:p fact]]
         ^{:key [fact length]}
         [:li
          [:p fact]]))])

(defn facts-list-antd []
  [list/list 
   {:item-layout "horizontal"
    :dataSource (get @state :facts)
    :render-item (fn [item]
                  ;;  (js/console.log item)
                  ;;  (prn item)
                   (r/as-element [list/list-item (.-fact item)])
                   )}])

;; How to make ajax call
(defn home-page []
  (r/create-class
   {:display-name "Http-request"
    :component-did-mount fetch-facts
    :reagent-render
    (fn []
      
      [:div
       [:div "hi"]
       [facts-list]
       [facts-list-antd]
       [:hr]
       [:pre [:code (with-out-str (cljs.pprint/pprint @state))]]]
      
      )}))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))

