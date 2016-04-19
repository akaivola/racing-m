(defproject controller "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374"]
                 [reagent "0.5.1" :exclusions [cljsjs/react]]
                 [re-frame "0.6.0"]
                 [prismatic/schema "1.0.4"]
                 [com.taoensso/timbre "4.3.1"]]
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-6"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js"]
  :aliases {"prod-build" ^{:doc "Recompile code with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once" "android"]]}
  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.0-2"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "env/dev"]
                   :cljsbuild    {:builds {:android {:source-paths ["src" "env/dev"]
                                                     :figwheel     true
                                                     :compiler     {:output-to     "target/android/not-used.js"
                                                                    :main          "env.android.main"
                                                                    :output-dir    "target/android"
                                                                    :parallel-build true
                                                                    :optimizations :none}}}}
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild {:builds {:android {:source-paths ["src" "env/prod"]
                                                   :compiler     {:output-to     "index.android.js"
                                                                  :main          "env.android.main"
                                                                  :output-dir    "target/android"
                                                                  :parallel-build true
                                                                  :optimizations :simple}}}}}})
