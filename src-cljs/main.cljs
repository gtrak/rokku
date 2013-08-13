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
  (.log js/console c))

(defn from-char
  [c]
  (log c)
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
  ([method url data] (xhr method url data identity))
  ([method url data success]
     (let [cfg (clj->js {:method (string/upper-case (name method))
                         :url url
                         :data data})
           shim-fn (fn [f data status headers config]
                     (f (js->clj {:data data
                                  :status status
                                  :headers headers
                                  :config config})))]
       (-> (($http) cfg)
           (.success (partial shim-fn success)))))) 

(defn post-key
  [key]
  (xhr :post (rel "/key") {:key key}))

(defn handle-key
  [e]
  (let [k (.-keyCode e)]
    (post-key (or (kmap k) 
                  (from-char k)))))

(defn ^:export button-press
  [key]
  (post-key (keyword key)))

(.addEventListener js/window "keydown" handle-key false)



