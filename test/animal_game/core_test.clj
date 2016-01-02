(ns animal-game.core-test
  (:require [clojure.test :refer :all]
            [animal-game.core :refer :all]))

(def mock-questions {
  "isTwolegged" "Два крака ли има?"
  "isWarmBlooded" "Топлокръвно ли е?"
})

(def mock-animals [
  {
     "name" "котка"
     "isTwolegged" false
     "isWarmBlooded" true
  }
  {
     "name" "орел"
     "isTwolegged" true
     "isWarmBlooded" true
  }
  {
     "name" "акула"
     "isTwolegged" false
     "isWarmBlooded" false
  }
])

(def mock-decision-tree {
  :attribute "isWarmBlooded"
  :true {
    :attribute "isTwolegged"
    :true '("орел")
    :false '("котка")
    }
  :false '("акула")
})

(deftest first-attribute-from-questions-positive
  (testing "Does first-attribute-from-questions return properly."
    (is (= (first-attribute-from-questions mock-questions)
           "isTwolegged"))))

(deftest build-decision-tree-simple
  (testing "build-decision-tree positive case"
    (is (= (build-decision-tree mock-animals (into '() mock-questions))
           mock-decision-tree))))

(deftest question->attribute-name-simple
  (testing "simple conversion from question to attrubute name"
    (is (= (question->attribute-name "a sample question")
           "a-sample-question"))))
