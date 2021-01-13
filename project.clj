(defproject hello-lambda-cljs "1.0.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.758"]
                 [org.clojure/core.async "1.3.610"]
                 [thheller/shadow-cljs "2.11.13"]
                 [cider/cider-nrepl "0.25.6"]]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :aliases {"shadow:repl" ["run" "-m" "shadow.cljs.devtools.cli" "--npm" "node-repl"]}
  :cljsbuild
  {:builds [{:source-paths ["src"]
             :compiler {:output-to "hello_lambda_cljs.js"
                        :main hello-lambda-cljs.core
                        :target :nodejs
                        :optimizations :simple
                        :npm-deps {:luxon "1.25.0"
                                   :aws-sdk "2.824.0"}
                        :install-deps true}}]})
