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

   [ajax.core :as ajax]
   [re-frame.core :as rf]
   [clojure.string :as string]))

;; -------------------------
;; Utils

(defn element-value
  "Gets the value of the targeted element"
  [e]
  (-> e .-target .-value))

(defn error-handler [{:keys [status status-text]}]
  "Print the error to the console"
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn vec-remove
  "remove elem at postion in coll"
  [coll pos]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

;; -------------------------
;; Subscriptions                     (normally put them into their own file)

;; Initialize the re-frame db
(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   (prn "DB initialized!")
   {:facts []}))

(rf/reg-event-db
 :set-facts
 (fn [db [name-of-event-handler new-facts]]
   (assoc db :facts new-facts)))

(rf/reg-event-db
 :delete-facts
 (fn [db [_ fact-idx]]
   (update db :facts vec-remove fact-idx)))

(rf/reg-sub
 :db
 (fn [db]
   db))

(rf/reg-sub
 :facts
 (fn [db]
   (get db :facts)))

(rf/reg-sub
 :fact
 :<- [:facts]
 (fn [facts [_ fact-idx]]
   (get-in facts [fact-idx "fact"])))

(rf/reg-sub
 :fact-word-count
 :<- [:facts]
 (fn [facts [_ fact-idx]]
   (count (string/split (get-in facts [fact-idx "fact"]) " "))))

;; Example of many sub subscriptions
(rf/reg-sub
 :fact-words-compare
 :<- [:facts]                          ;; this subscription is called first
 :<- [:fact-word-count 0]              ;; can hardcode parameters but not dynamically provide them
 (fn [[facts first-fact-words] [_ idx]]
   (int (* (/ (count (string/split (get-in facts [idx "fact"]) " "))
              first-fact-words)
           100))))

;; -------------------------
;; Views

(defn fetch-facts [this]
  ;; Calls "https://catfact.ninja/facts?limit=5"
  (ajax/GET "https://catfact.ninja/facts"
    {:params          {:limit 5}
     :response-format :json
     :handler         (fn [response]
                        ;; (prn response)
                        (rf/dispatch [:set-facts (get response "data")]))
     :error-handler   error-handler}))

;; The fix
(defn facts-list []
  [:ol
   (doall
    (for [fact-idx (range 0 (count @(rf/subscribe [:facts])))]
      ^{:key fact-idx}
      [:li
       [:div
        [:p @(rf/subscribe [:fact fact-idx])]
        [:p.red
         "Word Count :"
         @(rf/subscribe [:fact-word-count fact-idx])
         " | Lengthness "
         @(rf/subscribe [:fact-words-compare fact-idx])
         "%"
         [:button
          {:on-click (fn [_]
                       (rf/dispatch [:delete-facts fact-idx]))}
          "delete"]]]]))])

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
         [:hr]
         [:h3 "State"]
         [:pre [:code (with-out-str (cljs.pprint/pprint @(rf/subscribe [:db])))]]])})))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  (mount-root))

