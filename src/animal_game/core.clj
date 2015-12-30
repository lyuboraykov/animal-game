(ns animal-game.core
  (:gen-class))

 (require '[clojure.data.json :as json])
 (require '[clojure.java.io :as io])

(defn match-feature?
  "Return whether an item matches a feature"
  [item feature]
  (get item feature))

(defn last-feature-from-questions
  "Return the first property name of the questions"
  [questions]
  (first (keys (peek questions))))

(defn build-decision-tree
  "Builds decision tree from samples and questions"
  [samples questions]
  (let [feature (last-feature-from-questions questions)]
    (cond
      (= (count samples) 0) nil
      (= (count samples) 1) (map #(get % "name") samples)
      :else { :attribute feature
              :true (build-decision-tree
                     (filter #(match-feature? % feature)
                             samples)
                     (pop questions))
              :false (build-decision-tree
                      (filter #(not (match-feature? % feature))
                              samples)
                      (pop questions))})))

(defn json-file->samples
  "Return a map of all samples in the json file"
  []
  (json/read-str (slurp (io/resource "./samples.json"))))

(defn json-file->questions
  "Return a map of all questions in the json file"
  []
  (json/read-str (slurp (io/resource "./questions.json"))))

(defn -main
  "Starts the program."
  [& args]
  (let [samples (json-file->samples)
        questions (json-file->questions)]
    (println (build-decision-tree samples
                                  questions))))
