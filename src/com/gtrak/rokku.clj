(ns com.gtrak.rokku
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clj-http.client :as httpc]
            [clojure.core.async :as async :refer [go go-loop]])
  (:import [java.net DatagramSocket InetAddress DatagramPacket]))

(def m-search
  "M-SEARCH * HTTP/1.1
Host: 239.255.255.250:1900
Man: \"ssdp:discover\"
ST: roku:ecp
MX: 2

")

(defn send-m-search
  []
  (let [a (InetAddress/getByName "239.255.255.250")
        m-search (.getBytes m-search)]
    (with-open [s (DatagramSocket.)]
      (let [send-packet (DatagramPacket. m-search (count m-search) a 1900)
            rcv-bytes (byte-array 256)
            rcv-packet (DatagramPacket. rcv-bytes (count rcv-bytes))
            timeout (async/timeout 5000)]
        (.send s send-packet)
        (async/<!!
         (go-loop [responses []]
           (let [rcv-bytes (byte-array 256)
                 rcv-packet (DatagramPacket. rcv-bytes (count rcv-bytes))
                 receive (async/thread (.receive s rcv-packet))
                 [v p] (async/alts! [receive timeout])]
             (if (= p receive)
               (recur (conj responses (String. (.getData rcv-packet))))
               responses))))))))



(defn parse-response
  [response]
  (->> response
       s/split-lines
       (map (partial re-matches #"(.*): (.*)"))
       (keep (comp seq (partial drop 1)))
       (map vec)
       (into {})))

(defn roku-address
  [{:strs [Location LOCATION] :as parsed-response}]
  (java.net.URL. (or Location LOCATION)))

(def roku-keys
  '{:home Home
    :rev Rev
    :fwd Fwd
    :play Play
    :select Select
    :left Left
    :right Right
    :down Down
    :up Up
    :back Back
    :instant-replay Backspace
    :info Info
    :backspace InstantReplay
    :search Search
    :enter Enter
    :lit Lit_})

(defn request*
  [address button]
  (when-let [k (get roku-keys button)]
    (println k)
    (httpc/post (str address "keypress/" k))))

(defn encode
  [s]
  (java.net.URLEncoder/encode (str s) "UTF-8"))

(defn letter*
  [address l]
  (httpc/post (str address "keypress/" (get roku-keys :lit) (encode l))))

(def rokus (delay (map parse-response (send-m-search))))

(defn addresses
  []
  (map roku-address @rokus))
