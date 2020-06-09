(ns rewrap.compile.component-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]
            [rewrap.compile.component :as c]))

(deftest parse-args
  (testing "can use multiple parsers"
    (is (= (c/parse-args [:txt "Foo"]
                         {:parsers {:txt     {:tag "text"}
                                    keyword? {:tag #(name %)}
                                    any?     {:tag (fn [t] (string/capitalize t))}}})
           ["Text" nil ["Foo"]]))))
