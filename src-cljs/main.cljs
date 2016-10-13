(ns com.gtrak.rokku.main
  (:require [clojure.string :as string]
            [rum.core :as rum]))

(def kmap
  {36 :home
   188 :rev
   190 :fwd
   13 :select
   37 :left
   39 :right
   40 :down
   38 :up
   189 :back
   8 :backspace
   187 :play})

(comment :instant-replay
         :info
         :search
         :enter)

(defn log
  [c]
  (.log js/console c)
  c)

(defn from-char
  [c]
  (.fromCharCode js/String c))

(defn cached-singleton
  [f]
  (let [val (atom nil)]
    (fn []
      (or @val (reset! val (f))))))

(defn xhr
  ([method url data] (xhr method url data nil))
  ([method url data success]
   (let [cfg #js {:method (string/upper-case (name method))
                  :headers #js {:Content-Type "application/json"}
                  :body (if (= :get method)
                          nil
                          (js/JSON.stringify (clj->js data)))}
         shim-fn (fn [f response]
                   (and f (f (js->clj response :keywordize-keys true))))]
     (-> (js/window.fetch url cfg)
         (.then #(.text %))
         (.then (fn [text]
                  (if (seq text)
                    (js/JSON.parse text))))
         (.then (partial shim-fn success))))))

(defn rel
  [url]
  (str "/key") url)

(defn get-rokus
  [cb]
  (xhr :get "/rokus" nil cb))

(def state (atom {:current-key nil
                  :rokus []
                  :current-roku nil}))

(defn post-key
  [key]
  (let [{:keys [current-roku rokus]} @state
        address (or current-roku (first rokus))]
    (xhr :post (rel "/key")
         {:key key
          :address address})))

(defn do-t
  [ms f]
  (js/setTimeout f ms))

(defn handle-key
  [e]
  (let [k (.-keyCode e)
        key (or (kmap k)
                (from-char k))
        set-f #(swap! state assoc :current-key %)]
    (set-f (name key))
    (do-t 100 #(set-f nil))
    (post-key key)))


(.addEventListener js/window "keydown" handle-key false)

(def kbd-keys
  ["Home" "Home"
   "<" "Rewind"
   ">" "Fast-Forward"
   "Enter" "Select"
   "Left-Arrow" "Left"
   "Right-Arrow" "Right"
   "Down-Arrow" "Down"
   "Up-Arrow" "Up"
   "- (minus)" "Back"
   "Backspace" "Instant Replay"
   "= (equals)" "Play/pause"])

(rum/defc button
  [{:keys [icon key]} & more]
  [:btn.btn.span4 {:id key
                   :on-click #(post-key key)}
   [:i {:class icon}]
   more])

(rum/defc group
  [& more]
  [:div.btn-group.row-fluid more])

(rum/defc roku-remote < rum/reactive []
  (let [state (rum/react state)]
    [:div
     [:div.span4
      [:table.table.table-striped.table-bordered
       [:thead
        [:caption "Legend"]
        [:tr
         [:th "Keyboard-Key"]
         [:th "function"]]]
       [:tbody
        (for [[k function] (partition 2 kbd-keys)]
          [:tr
           [:td k]
           [:td function]])]]]

     [:div.span6
      (group
       (button {:icon "icon-chevron-left"
                :key "back"}
               "Back")
       (button {:icon "icon-arrow-up"
                :key "up"})
       (button {:icon "icon-home"
                :key "home"}
               "Home"))
      (group
       (button {:icon "icon-arrow-left"
                :key "left"})
       (button {:icon "icon-ok"
                :key "select"}
               "OK")
       (button {:icon "icon-arrow-right"
                :key "right"}))
      (group
       (button {:icon "icon-step-backward"
                :key "backspace"}
               "Replay")
       (button {:icon "icon-arrow-down"
                :key "down"})
       (button {:icon "icon-asterisk"
                :key "info"}))
      [:div {:style #js {:height "20px"}}]
      (group
       (button {:icon "icon-backward"
                :key "rev"})
       (button {:icon "icon-play"
                :key "play"}
               [:i.icon-pause])
       (button {:icon "icon-fast-forward"
                :key "fwd"}))
      [:div {:style #js {:height "100px"}}]
      (:rokus state)
      [:div {:style #js {:float "none"}}
       [:h1 {:style #js {:font-size "850%"}}
        (:current-key state)]]]]))

(rum/mount (roku-remote) js/document.body)

(get-rokus #(swap! state assoc :rokus (:addresses %)))

