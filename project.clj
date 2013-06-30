(defproject rokku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.2"]
                 [compojure "1.1.5"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [ring/ring-core "1.1.8"]
                 [ring-middleware-format "0.3.0"]]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/main.js"  ; default: target/cljsbuild-main.js
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :main com.gtrak.rokku.server
  :warn-on-reflection true)
