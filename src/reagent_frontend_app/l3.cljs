(ns reagent-frontend-app.l3
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
  "Print the error to the console"
  (.log js/console (str "something bad happened: " status " " status-text)))

;; -------------------------
;; Views

(def state
  (r/atom {:facts []}))

(defn fetch-facts [this]
  ;; Calls "https://catfact.ninja/facts?limit=5"
  (ajax/GET "https://catfact.ninja/facts"
    {:params          {:limit 5}
     :response-format :json
     :handler         (fn [response]
                        ;; (prn response)
                        (swap! state assoc :facts (get response "data")))
     :error-handler   error-handler}))

;; When dereferencing an atom in a loop, should wrap it in a doall
;; otherwise you get this error "Reactive deref not supported in lazy seq, it should be wrapped in doall"
#_(defn facts-list []
    [:ol
   ;; Can use a for each loop here, but using index for the sake of the example
     (for [idx (range 0 (count (get @state :facts)))]
       ^{:key idx}
       [:li
        [:p (get-in @state [:facts idx "fact"])]])])

;; The fix
#_(defn facts-list []
    [:ol
     (doall ;; add doall
      (for [idx (range 0 (count (get @state :facts)))]
        ^{:key idx}
        [:li
         [:p (get-in @state [:facts idx "fact"])]]))])

;; This fix also works - but not sure what the trade offs are
#_(defn facts-list []
    (into [:ol]
          (for [idx (range 0 (count (get @state :facts)))]
            ^{:key idx}
            [:li
             [:p (get-in @state [:facts idx "fact"])]])))

;; Example of seperate branch keying
;; Error "Every element in a seq should have a unique :key"
#_(defn facts-list []
    [:ol
     (for [{:strs [fact length]} (get @state :facts)]
       ^{:key fact}
      ;; FYI this is not how you should be toggling css
       (if (> length 100)
         [:li.red [:p fact]]
         [:li [:p fact]]))])

;; The fix
(defn facts-list []
  [:ol
   (for [{:strs [fact length]} (get @state :facts)]
     (if (> length 100)
       ^{:key fact} ;; add key per branch
       [:li.red 
        [:p fact]]
       ^{:key [fact length]} ;; add key per branch, can be a different unique key 
       [:li 
        [:p fact]]))])

;; ANTD List
(defn facts-list-antd []
  (let [facts (get @state :facts)]
    [list/list
     {:item-layout :horizontal
      ;; Header expects a ReactNode so must do a convertion from the reagent component
      ;; For fields that require a ReactNode object, can use this method
      ;; to convert reagent components into one
      :header      (r/as-element [:h3 "Facts"])
      ;; dataSource takes in a vector of strings or a vector of maps
      ;; NOTE: dataSource is the only field required to be in camelBack notation from syn-antd
      :dataSource  (get @state :facts)
      :loading     (nil? facts)
      ;; renderItem expects  (item) -> ReactNode
      :renderItem  (fn [item] 
                     ;; maps and structures provided by ANTD functions will be in js 
                     ;; so item is an object with {"fact" "..." "length" #}
                     
                     ;;  (js/console.log item)
                     ;;  (prn item)
                     
                     (r/as-element
                      [list/list-item 
                       ;; can use js interop to access value. 
                       ;; However, shadow-cljs will complain as the method is only avaliable at runtime
                       [:p (.-fact item)]]))}]))

;; How to make a http call
(defn home-page []
  (let []
    (r/create-class
     {:display-name         "ajax-example"
      :component-did-mount  fetch-facts
      :reagent-render
      (fn []
        [:div
         [:h2 "Http Request"]
         [facts-list]
         [facts-list-antd]
         [:hr]
         [:h3 "State"]
         [:pre [:code (with-out-str (cljs.pprint/pprint @state))]]])})))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))

