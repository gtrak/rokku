(ns com.gtrak.rokku.server
  (:require [compojure.route :as route]
            [compojure.core :refer [routes GET POST]]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [cheshire.core :as cheshire]
            [me.raynes.laser :as laser]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.format-params :as ring-fparams]
            [ring.middleware.params :as ring-params]
            [ring.middleware.stacktrace :as st]
            [com.gtrak.rokku :as rokku]
            [ring.util.response :as rr]))


(defn accept-key
  [key]
  (if (= 1 (count key))
    (rokku/letter key)
    (rokku/request (keyword key))))

(defn rel
  [context url]
  (let [url (str url)]
    (if (or (-> url java.net.URI. .isAbsolute)
            (.startsWith url "//")
            ;; catches path-relative urls
            (not (.startsWith url "/")))
      url
      (str context url))))

(defn add-context-fn
  [attr context]
  (fn [node]
    (update-in
     node
     [:attrs attr]
     (partial rel context))))

(defn relativize
  [content context]
  (if-not context
    content
    (laser/document
     (laser/parse content)
     (laser/element= :a) (add-context-fn :href context) 
     (laser/element= :link) (add-context-fn :href context)
     (laser/element= :script) (add-context-fn :src context))))

(defn html [path]
  (content-type/wrap-content-type
   (GET path {context :context}
        (if-let [r (io/resource (str "public/" path))]
          (rr/response (-> (slurp r)
                           (relativize context)))))))

(defmacro wrap-request-binding
 "Wraps the ring handler definition to provide request-scoped bindings
  via symbol-capture on %."
  [bindings handler-def]
  `(fn [request#]
     (let [~'% request#
           ~@bindings]
       (~handler-def request#))))

(defn make-handler
  []
  (wrap-request-binding
   [{:keys [context]} %]
   (routes
    ;; static
    (POST "/key" [key]
          (accept-key key)
          (rr/response nil))
    (GET "/favicon.ico" [] (rr/resource-response "public/favicon.ico"))
    (html "/index.html")
    (GET "/" [] (rr/redirect (rel context "/index.html")))
    (content-type/wrap-content-type
     (GET "/properties.js" []
          (str "var properties="
               (cheshire/generate-string {:context context})
               ";")))
    (route/resources "/")
    (rr/not-found nil))))

(def handler
  (-> (make-handler)
      ring-params/wrap-params
      (ring-fparams/wrap-json-params :decoder #(cheshire/parse-string % true))
      st/wrap-stacktrace-web))

(comment
  (alter-var-root (var *out*) (constantly *out*))
  )

