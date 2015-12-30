(ns animal-game.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Is it a dog?")
  (let [input (read-line)]
    (if (or (= input "y") (= input "yes"))
    (println "It's a dog")
    (println "It's not a dog")
    )))
