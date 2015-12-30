(ns animal-game.core
  (:gen-class))

(defn animal-found? [] false)

(def root-question "Is it a cat?")

(defn save-answer
    "Save the answer of the question in the JSON file"
    [decision-tree question answer]
    (println "Answer saved!"))

(defn next-question
  "Get the next question to be asked"
  [decision-tree]
  root-question)

(defn ask-question
  "Ask the user to provide an aswer for a question."
  [decision-tree]
  (while (not (animal-found?))
    (let [question (next-question decision-tree)](
      (println question)
      (let [answer (read-line)]
      (save-answer decision-tree question answer))))))


(defn -main
    "Simple user input and output."
    [& args]
    (ask-question {}))
