(ns rewrap.util.casing
  (:require [clojure.string :as string]))

(defn camel->lisp "Convert camel-case string `s` to lisp-case."
  [s]
  (-> s
      (string/replace #"(.)([A-Z][a-z]+)" "$1-$2")
      (string/replace #"([a-z0-9])([A-Z])" "$1-$2")
      (string/lower-case)))
