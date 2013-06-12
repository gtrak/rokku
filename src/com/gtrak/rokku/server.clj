(ns com.gtrak.rokku.server
  (:require [compojure.route :as route]
            [compojure.core :refer [routes GET POST]]
            [cheshire.core :as cheshire]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.format-params :as ring-fparams]
            [ring.middleware.params :as ring-params]
            [ring.middleware.stacktrace :as st]
            [com.gtrak.rokku :as rokku]
            [ring.util.response :as rr])
  (:gen-class :main true))


(defn accept-key
  [key]
  (if (= 1 (count key))
    (rokku/letters [key])
    (rokku/request (keyword key))))

(defn make-handler
  []
  (routes
   ;; static
   (POST "/key" [key]
         (accept-key key)
         (rr/response nil))
   (GET "/favicon.ico" [] (rr/resource-response "public/favicon.ico"))
   (route/resources "/")))

(def handler
  (-> (make-handler)
      ring-params/wrap-params
      (ring-fparams/wrap-json-params :decoder #(cheshire/parse-string % true))
      st/wrap-stacktrace-web))

(defn -main [& params]
  (future (run-jetty #'handler
                     {:port 8089})))
;;(-main)
