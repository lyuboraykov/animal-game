(ns animal-game.core
  (:gen-class))

 (require '[clojure.data.json :as json])
 (require '[clojure.java.io :as io])

(def animals-file-path "./animals.json")
(def questions-file-path "./questions.json")

(defn first-attribute-from-questions
  "Return the first attribute name of the questions"
  [questions-list]
  (first (first questions-list)))

(defn build-decision-tree
  "Builds decision tree from animals and questions-list"
  [animals questions-list]
  (let [attribute (first-attribute-from-questions questions-list)]
    (cond
      (empty? animals) nil
      (= (count animals) 1) (map #(get % "name") animals)
      :else {
              :attribute attribute
              :true (build-decision-tree
                      (filter #(get % attribute true) animals)
                      (rest questions-list))
              :false (build-decision-tree
                       ; when an attribute is missing, the animal should appear
                       ; on both sides of the tree, thus the false value of get
                       (filter #(not (get % attribute false)) animals)
                       (rest questions-list))
            })))

(defn json-file->object
  "Read a json file into an object"
  [file-path]
  (json/read-str (slurp (io/resource file-path))))

(defn get-boolean-answer
  "Get a true/false answer from the user console input"
  []
  (let [answer (read-line)]
    (boolean (re-matches #"^ *[Дд][аА] *$" answer))))

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
                           animals-file-path)
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
        (def current-question (get questions (:attribute decision-tree)))
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

(defn add-animal-attribute
  "Add an animal which has the same attributes as another one except a new one"
  [animals original-animal animal-name attribute-name attribute-value]
  (let [new-animal (assoc original-animal "name" animal-name
                                          attribute-name attribute-value)
        modified-old-animal (assoc original-animal attribute-name
                                                   (not attribute-value))]
    (def animals-to-write (filter #(not (= (get % "name")
                                           (get original-animal "name")))
                                  animals))
    (write-object-to-file (conj animals-to-write new-animal modified-old-animal)
                          animals-file-path)))

(defn add-new-question
  "Add a new question to the library"
  [questions attribute question]
  (write-object-to-file (assoc questions attribute question)
                        questions-file-path))

(defn add-ambiguous-animal
  "Add a new animal which has the same properties as an already existing one."
  [animal animals questions]
  (println "Предавам се. Кое е твоето животно?")
  (def new-animal-name (read-line))
  (println (str "Какво да питам за да го различа от " (get animal "name") "?"))
  (def new-question (read-line))
  (println (str new-animal-name " отговаря ли с 'Да' на въпроса?"))
  (let [new-attribute-value (get-boolean-answer)
        new-attribute-name (question->attribute-name new-question)]
    (add-new-question questions new-attribute-name new-question)
    (add-animal-attribute animals animal new-animal-name
                          new-attribute-name new-attribute-value))
  (println "Благодаря, запомних!"))

(defn -main
  "Starts the program."
  [& args]
  (let [animals (json-file->object animals-file-path)
        questions (json-file->object questions-file-path)]
    (def decision-tree (build-decision-tree animals
                                            (into '() questions)))
    (def animal (guess-animal decision-tree questions))
    (if (nil? (get animal "name"))
      (add-unknown-animal animals animal)
      (if (animal-correct? (get animal "name"))
        (println "Благодаря, че играхме!")
        (add-ambiguous-animal animal animals questions)))))
