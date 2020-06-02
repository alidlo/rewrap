(ns rewrap.compile
  (:require [rewrap.compile.component :as component]
            [rewrap.compile.body :as body]))

(defn hiccup
  ([body opts] (hiccup (assoc opts :body body)))
  ([{:keys [compiler body] :as opts}]
   (let [tag  (first body)
         args (rest body)]
     `(~compiler ~tag ~@(component/parse-args
                         args
                         {:parse-child (fn [ch]
                                         (println ch)
                                         (cond 
                                           (vector? ch) (hiccup ch opts)
                                           (list? ch) (body/parse-expr-output ch #(hiccup % opts))
                                           :else ch
                                           ))})))))

(comment
  (require '[clojure.walk])
  (do
    (def xpand clojure.walk/macroexpand-all)

    (defmacro compiler [tag props & children]
      `(.createElement react ~(name tag) ~props ~@children))

    (defmacro h [body]
      (hiccup {:compiler `compiler
               :body body})))

  (xpand '(h [:txt "Hello"]))
  (xpand '(h [:vw [:txt "Hello"]]))
  (xpand '(h [:vw [:txt "Hello"] [:txt "World"]]))
  (xpand '(h [:vw [:vw [:txt "hello"]]]))
  (xpand '(h [:vw (if bool [:txt "Yes"] [:txt "No"])])))

