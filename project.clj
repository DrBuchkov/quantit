(defproject quantit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/core.match "1.0.0"]
                 [com.stuartsierra/component "1.0.0"]
                 [com.yahoofinance-api/YahooFinanceAPI "3.15.0"]
                 [camel-snake-kebab "0.4.1"]
                 [tick "0.4.26-alpha"]

                 ;; TODO: Maybe not needed
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                 [org.apache.commons/commons-lang3 "3.10"]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :repl-options {:init-ns quantit.sandbox}
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[midje "1.9.9"]
                                      [org.clojure/test.check "1.1.0"]]}
             })
