(ns reagent-frontend-app.l1
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]

   [syn-antd.input :as input]
   [syn-antd.button :as button]))

;; -------------------------
;; Utils

(defn element-value
  "Gets the value of the targeted element"
  [e]
  ; The value of the element is in myElement.target.value

  ; Way #1
  ; We can access the an object's attributes/method one level at a time to get it
  ;; (.-value (.-target  e))

  ; Way #2 
  ; Using the thread first macro, this reads much more nicely
  ;; (-> e .-target .-value)

  ; Way #3
  ; Using syntax sugar, the .. function provides a shorter equivalent
  (.. e -target -value)

  ; NOTES: 
  ;   .-field accesses a field
  ;   .method calls a method 

  ; i.e. myElement.target.toString()
  ; (->> e .-target .toString)
  )

;; -------------------------
;; Views

;; Intro to hiccup, 
#_(defn home-page []
  [:div
   [:h2
    {:style {:background-color :lightblue
             :padding "0.5em"}}
    "Welcome to Reagent"]])

;; Intro to console, and js interop to access the DOM 
#_(defn home-page []
    [:div
     [:h2 "Welcome to Reagent"]
     [input/input
      {:on-change (fn [element]
                    ; (prn element)
                    ; (js/console.log element)
                    (prn (element-value element)))}]])

;; Example of using form 2 to make local component state
#_(defn home-page []
    (let [value (r/atom "")]
      (fn []
        [:div
         [:h2 "Welcome to Reagent"]
         [:p [:span "Value: "] [:span @value]]
         [input/input
          {:value     @value
           :on-change (fn [element]
                        (reset! value (element-value element)))
         ;; Most basic methods from HTML are still usable even though that might not be listed in the API 
           #_#_:on-blur   (fn [_]
                            (js/alert "You left the field"))}]])))

;; Example of form 2 mistake
#_(defn my-label [value]
    (fn [#_value]
      [:p [:span "my label: "] [:span value]]))

#_(defn home-page []
    (let [value (r/atom "Hello")]
      (fn []
        [:div
         [:h2 "Welcome to Reagent"]
         [:p [:span "Value: "] [:span @value]]
         [input/input
          {:value     @value
           :on-change (fn [element]
                        (reset! value (element-value element)))}]
         [my-label @value]])))

;; Exmaple of form 3
#_(defn form3-example [message]
    (let []
      (r/create-class                       ;; <-- expects a map of functions 
       {:display-name  "form3-example"      ;; <-- names the object class

        :component-did-mount                ;; the name of a lifecycle function
        (fn [this]
          (println "component-did-mount" this)) ;; your implementation

        :component-did-update               ;; the name of a lifecycle function
        (fn [this old-argv]                 ;; reagent provides you the entire "argv", not just the "props"
          (let [new-argv (rest (r/argv this))]
            (js/console.log "component-did-update" this old-argv new-argv)))

        :component-will-unmount
        (fn [this]
          (println "component-will-unmount" this))

        :reagent-render                     ;; Note:  is not :render
        (fn [message]                       ;; remember to repeat parameters
          [:div
           [:h2 "form3-example"]
           [:p message]])})))

#_(defn home-page []
    (let [value (r/atom "This is form 3")
          show? (r/atom true)]
      (fn []
        [:div
         [:h2 "Welcome to Reagent"]
         [:p [:span "Value: "] [:span @value]]
         [input/input
          {:value     @value
           :on-change (fn [element]
                        (reset! value (element-value element)))}]
         [button/button
          {:on-click #(swap! show? not)}
          (if @show? "Hide" "Show")]
         [:br]
         [:br]                            ;; DO NOT USE BR - BAD PRACTICE
         (when @show?
           [form3-example @value])])))

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
