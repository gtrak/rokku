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
  (.log js/console (str c)))

(defn from-char
  [c]
  (log c)
  (.fromCharCode js/String c))

(defn xhr [[method uri] content callback]
  (let [params (clj->js {:type (string/upper-case (name method))
                         :dataType "json"
                         :data (clj->js content)
                         :success callback})]
    (.ajax js/jQuery uri params)))

(defn handle-keys
  [e]
  (let [k (.-keyCode e)]
    (xhr [:post "/key"]
         {:key (or (kmap k)
                   (from-char k))}
         identity)))

(.addEventListener js/window "keydown" handle-keys false)


