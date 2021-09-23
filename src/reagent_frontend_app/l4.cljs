(ns reagent-frontend-app.l4
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

;; reg-event-db denotates an event handler that performs some sort
;; of db side effect
(rf/reg-event-db
 :set-facts
 ;; you don't usually need the `name-of-this-effect`, in this case it would just be :set-facts
 (fn [db [name-of-this-effect new-facts]]
   (prn "Updated facts!")
   (assoc db :facts new-facts)))

;; Subscriptions to read data from the db
(rf/reg-sub
 :db                                 ;; name of the subscription
 (fn [db]
   db))

(rf/reg-sub
 :facts
 (fn [db]
   (get db :facts)))

;; --

(rf/reg-sub
 :fact
 (fn [db [name-of-this-sub idx]]
   (get-in db [:facts idx "fact"])))

;; Example of sub subscriptions that utilize existing subscriptions
(rf/reg-sub
 :fact-word-count
 :<- [:facts]                          ;; this subscription is called first
 (fn [facts [_ idx]]                   ;; the result will be given in the first argument
   (count (string/split (get-in facts [idx "fact"]) " "))))

;; Example of many sub subscriptions
(rf/reg-sub
 :fact-words-compare
 :<- [:facts]                          ;; this subscription is called first
 :<- [:fact-word-count 0]              ;; can hardcode parameters but not dynamically provide them
 ;; if there are multiple subs-subscriptions, the first argument will a vector of the results
 ;; NOTE: this is a poor practical example
 (fn [[facts first-fact-words] [_ idx]]
   (int (* (/ (count (string/split (get-in facts [idx "fact"]) " "))
              first-fact-words)
           100))))

;; --

(rf/reg-event-db
 :delete-fact
 ;; you don't usually need the `name-of-this-effect`, in this case it would just be :set-facts
 (fn [db [_ fact-idx]]
   (update db :facts vec-remove fact-idx)))

;; -------------------------
;; Views

(defn fetch-facts []
  (ajax/GET "https://catfact.ninja/facts"
    {:params {:limit 5}
     :response-format :json
     :handler (fn [response]
                (rf/dispatch [:set-facts (get response "data")]))
     :error-handler error-handler}))

#_(defn facts-list []
    [:ol
     (for [{:strs [fact length]} @(rf/subscribe [:facts])]
       ^{:key fact}
       [:li
        [:p fact]])])

(defn facts-list []
  @(rf/subscribe [:fact :a :b])
  [:ol
   (doall
    (for [fact-idx (range 0 (count @(rf/subscribe [:facts])))]
      ^{:key fact-idx}
      [:li
       [:div
        [:p @(rf/subscribe [:fact fact-idx])]
        [:p.red
         "Word Count: "
         @(rf/subscribe [:fact-word-count fact-idx])
         " | Size compared to first fact: "
         @(rf/subscribe [:fact-words-compare fact-idx])
         "%"]
        [form/form-item
         [button/button
          {:on-click #(rf/dispatch [:delete-fact fact-idx])
           :type :danger}
          "delete"]]]]))])

;; How to make ajax call
(defn home-page []
  (r/create-class
   {:display-name "Http-request"
    :component-did-mount fetch-facts
    :reagent-render
    (fn []
      [:div
       [:h1 "Cat Facts"]
       [facts-list]
       [:hr]
       [:pre [:code (with-out-str (cljs.pprint/pprint @(rf/subscribe [:db])))]]])}))

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  ;; dispatch-sync is a synchronous version of dispatch so the 
  ;; call will be done immediately, use this minimally
  (rf/dispatch-sync [:initialize-db])
  (mount-root))
