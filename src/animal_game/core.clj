(ns animal-game.core
  (:gen-class))

 (require '[clojure.data.json :as json])
 (require '[clojure.java.io :as io])

(defn match-feature?
  "Return whether an item matches a feature"
  [item feature]
  (get item feature true))

(defn first-feature-from-questions
  "Return the first property name of the questions"
  [questions-list]
  (first (first questions-list)))

(defn build-decision-tree
  "Builds decision tree from samples and questions-list"
  [samples questions-list]
  (let [feature (first-feature-from-questions questions-list)]
    (cond
      (empty? samples) nil
      (= (count samples) 1) (map #(get % "name") samples)
      :else { :attribute feature
              :true (build-decision-tree
                     (filter #(match-feature? % feature)
                             samples)
                     (rest questions-list))
              :false (build-decision-tree
                      (filter #(not (match-feature? % feature))
                              samples)
                      (rest questions-list))})))

(defn json-file->samples
  "Return a map of all samples in the json file"
  []
  (json/read-str (slurp (io/resource "./samples.json"))))

(defn json-file->questions
  "Return a map of all questions in the json file"
  []
  (json/read-str (slurp (io/resource "./questions.json"))))

(defn attribute->question
  "Return a question string from the given decision tree attribute"
  [attribute questions]
  (get questions attribute))

(defn get-boolean-answer
  "Get a true/false answer from the user console input"
  []
  (let [answer (read-line)]
    (re-matches #"^ *[Дд][аА] *$" answer)))

(defn guess-animal
  "Ask questions from the decision tree until the animal
  is guessed or we are out of questions"
  [decision-tree questions]
  (if (= (count decision-tree ) 1)
    (first decision-tree)
    (do
      (def current-question (attribute->question
                            (:attribute decision-tree)
                            questions))
      (println current-question)
      (if (get-boolean-answer)
        (guess-animal (:true decision-tree) questions)
        (guess-animal (:false decision-tree) questions)))))

(defn -main
  "Starts the program."
  [& args]
  (let [samples (json-file->samples)
        questions (json-file->questions)]
    (def decision-tree (build-decision-tree samples
                                            (into '() questions)))
    (def animal (guess-animal decision-tree questions))
    (println (str "Животното " animal " ли е?"))))
