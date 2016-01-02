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
  "Builds decision tree from animals and questions-list"
  [animals questions-list]
  (let [feature (first-feature-from-questions questions-list)]
    (cond
      (empty? animals) nil
      (= (count animals) 1) (map #(get % "name") animals)
      :else { :attribute feature
              :true (build-decision-tree
                     (filter #(match-feature? % feature)
                             animals)
                     (rest questions-list))
              :false (build-decision-tree
                      (filter #(not (match-feature? % feature))
                              animals)
                      (rest questions-list))})))

(defn json-file->animals
  "Return a map of all animals in the json file"
  []
  (json/read-str (slurp (io/resource "./animals.json"))))

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

(defn write-object-to-file
  "Persist an object to a json file"
  [object json-file-path]
  (spit (io/resource json-file-path) (json/write-str object)))

(defn add-unknown-animal
  "Add a new animal to the database by asking questions about it"
  [animals animal]
  (println "Не познавам това животно. Как се казва?")
  (let [animal-name (read-line)]
    (write-object-to-file (conj animals (assoc animal "name" animal-name))
                           "./animals.json")
    (println "Животното бе добавено!")))

(defn guess-animal
  "Ask questions from the decision tree until the animal
  is guessed or we are out of questions"
  [decision-tree questions]
  (loop [decision-tree decision-tree animal-properties {}]
    (cond
      (nil? decision-tree) (assoc animal-properties "name" nil)
      (= (count decision-tree) 1) (assoc animal-properties
                                         "name" (first decision-tree))
      :else (do
        (def current-question (attribute->question
                              (:attribute decision-tree)
                              questions))
        (println current-question)
        (if (get-boolean-answer)
          (recur (:true decision-tree)
                 (assoc animal-properties
                        (:attribute decision-tree) true))
          (recur (:false decision-tree)
                 (assoc animal-properties
                        (:attribute decision-tree) false)))))))

(defn animal-correct?
  "Ask the user if this is the right animal"
  [animal-name]
  (println (str "Животното " animal-name " ли е?"))
  (get-boolean-answer))

(defn question->attribute-name
  "Get an attribute name from a full question"
  [question]
  (clojure.string/replace question #" " "-"))

(defn add-new-animal-characteristic
  "Add a new question to differentiate new animal from the old one"
  [animal animals questions]
  (println "Предавам се. Кое е твоето животно?")
  (def new-animal (read-line))
  (println (str "Какво да питам за да го различа от " (get animal "name") "?"))
  (def new-question (read-line))
  (println "Благодаря, запомних!"))

(defn -main
  "Starts the program."
  [& args]
  (let [animals (json-file->animals)
        questions (json-file->questions)]
    (def decision-tree (build-decision-tree animals
                                            (into '() questions)))
    (def animal (guess-animal decision-tree questions))
    (if (nil? (get animal "name"))
      (add-unknown-animal animals animal)
      (if (animal-correct? (get animal "name"))
        (println "Благодаря, че играхме!")
        (add-new-animal-characteristic animal animals questions)))))
