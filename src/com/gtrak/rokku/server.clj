(ns com.gtrak.rokku.server
  (:require [compojure.route :as route]
            [compojure.core :refer [routes GET POST]]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [cheshire.core :as cheshire]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.format-params :as ring-fparams]
            [ring.middleware.params :as ring-params]
            [ring.core.protocols :as ring-prot]
            [ring.middleware.stacktrace :as st]
            [ring.adapter.jetty :as ring-server]
            [com.gtrak.rokku :as rokku]
            [ring.util.response :as rr]))

(defn accept-key
  [address key]
  (if (= 1 (count key))
    (rokku/letter* address key)
    (rokku/request* address (keyword key))))

(extend-protocol ring-prot/StreamableResponseBody
  clojure.lang.IPersistentMap
  (ring-prot/write-body-to-stream [body response output-stream]
    (ring-prot/write-body-to-stream (cheshire/generate-string body) response output-stream)))

(defn make-handler
  []
  (routes
   (POST "/key" [address key]
         (accept-key address key)
         (rr/response nil))
   (GET "/rokus" []
        (rr/response {:addresses (map str (rokku/addresses))}))
   (GET "/" []
        (rr/resource-response "public/index.html"))
   (route/resources "/")
   (rr/not-found nil)))

(def handler
  (-> (make-handler)
      ring-params/wrap-params
      (ring-fparams/wrap-json-params :decoder #(cheshire/parse-string % true))
      st/wrap-stacktrace-web))

(defonce the-server (atom nil))

(defn start
  []
  (reset! the-server
          (ring-server/run-jetty #'handler {:port 3030
                                            :join? false})))

(defn stop
  []
  (.stop @the-server))

(comment
  (alter-var-root (var *out*) (constantly *out*))
  )

