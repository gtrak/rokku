(ns com.gtrak.rokku
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clj-http.client :as httpc])
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
        m-search (.getBytes m-search)
        rcv-bytes (byte-array 256)]
    (with-open [s (DatagramSocket.)]
      (let [send-packet (DatagramPacket. m-search (count m-search) a 1900)
            rcv-packet (DatagramPacket. rcv-bytes (count rcv-bytes))]
        (.send s send-packet)
        (.receive s rcv-packet)
        (String. (.getData rcv-packet))))))

(defn find-roku
  []
  (let [response (send-m-search)
        prefix "Location: "]
    (-> response
        s/split-lines
        (->> (filter #(.contains % prefix)))
        first
        (subs (count prefix))
        java.net.URL.)))

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

(def the-address (delay (find-roku)))

(defn request
  [button]
  (request* @the-address button))

(defn letter
  [letter]
  (letter* @the-address letters))
