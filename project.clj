(defproject animal-game "0.1.0-SNAPSHOT"
  :description "Simple decision-tree learning game"
  :url "https://github.com/lyuboraykov/animal-game"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :main ^:skip-aot animal-game.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
