(defproject rokku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [clj-http "3.3.0"]
                 [compojure "1.5.1" :exclusions [ring/ring-core]]
                 [ring/ring "1.6.0-beta6"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.2.391"]
                 [rum "0.10.7"]
                 [ring-middleware-format "0.7.0"]]

  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/main.js"
                                   :output-dir "resources/public/cljs"
                                   :optimizations :simple
                                   :externs ["externs.js"]
                                   :source-map "resources/public/main.js.map"
                                   }}]}
  :main com.gtrak.rokku.server

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :plugins [[lein-cljsbuild "1.1.4"]]
  )
