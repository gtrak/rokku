(ns com.gtrak.rokku.main
  (:require [clojure.string :as string]))

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

;;; load the properties
(def properties (js->clj js/properties :keywordize-keys true))

(defn rel
  [s]
  (str (:context properties) s))

(defn ng-root
  []
  (.getElementById js/document "root"))

(defn injector
  []
  (->> (ng-root)
       (.element js/angular)
       (.injector)))

(defn scope
  [elt]
  (->> elt
       (.element js/angular)
       (.scope)))

(defn service
  [svc]
  (.get (injector) svc))

(defn cached-singleton
  [f]
  (let [val (atom nil)]
    (fn []
      (or @val (reset! val (f))))))

(def $http (cached-singleton #(service "$http")))

;; main.http()({method:'GET',url:'/'})
;;   .success(function(data, status, headers, config){
;;     console.log(data)
;;   })
(defn xhr
  ([method url data] (xhr method url data nil))
  ([method url data success]
     (let [cfg (clj->js {:method (string/upper-case (name method))
                         :url url
                         :data data})
           shim-fn (fn [f data status headers config]
                     (and f (f (js->clj {:data data
                                         :status status
                                         :headers headers
                                         :config config}))))]
       (-> (($http) cfg)
           (.success (partial shim-fn success)))))) 

(defn post-key
  [key]
  (xhr :post (rel "/key") {:key key}))

(def state (cached-singleton #(service "state")))

(def root-scope (cached-singleton #(scope (ng-root))))

(defn update
  []
  (.$apply (root-scope)))

(defn do-t
  [ms f]
  (js/setTimeout f ms))

(defn handle-key
  [e]
  (let [k (.-keyCode e)
        _ (log k)
        key (or (kmap k) 
                (from-char k))
        state (state)
        set-f #(aset state "currentKey" %)]
    (set-f (name key))
    (update)
    (do-t 100 (fn []
                (set-f nil)
                (update)))
    (post-key key)))

(defn ^:export button-press
  [key]
  (post-key (keyword key)))

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

(defn ^:export legend
  []
  (clj->js (partition 2 kbd-keys)))

