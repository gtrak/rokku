(defproject rokku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.2"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.1.8"]
                 [ring-middleware-format "0.3.0"]
                 [me.raynes/laser "1.1.1"]
                 [org.jsoup/jsoup "1.7.2"]]
  :profiles {:dev {:dependencies [[clojure-complete "0.2.3"]]}}

  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/main.js"  ; default: target/cljsbuild-main.js
                                   :optimizations :advanced
                                   ;:pretty-print true
                                   }}]}
  :main com.gtrak.rokku.server
         
  :plugins [[lein-cljsbuild "0.3.2"]
            [lein-ring "0.8.5"]]

  :ring {:handler com.gtrak.rokku.server/handler
         :nrepl {:start? true
                 ;:port
                 }}
  
  :warn-on-reflection true)
