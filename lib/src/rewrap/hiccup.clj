(ns rewrap.hiccup
  (:refer-clojure :exclude [compile])
  (:require [rewrap.compile.component :as component]
            [rewrap.compile.body :as body]))

(defn wrap-child 
  "Helper for wrapping component args child `ch` in `compiler` macro.
   Compiler should accept same format designated in [compile]."
  [compiler ch]
  (if (vector? ch) `(~compiler ~@ch) ch))

(defn compile
  "Compile any hiccup in component `body` using custom `compiler`.
   Compiler macro should accept [tag props & children]. Children are destructured as that's how they appear in body."
  ([body opts] (compile (assoc opts :body body)))
  ([{:keys [compiler body] :as opts}]
   (let [tag  (first body)
         args (rest body)]
     `(~compiler ~tag ~@(component/parse-args
                         args
                         {:parse-child (fn [ch]
                                         (cond 
                                           (vector? ch) (compile ch opts)
                                           (list? ch) (body/parse-expr-output ch #(compile % opts))
                                           :else ch
                                           ))})))))


(comment
  (require '[clojure.walk])
  (do
    (def xpand clojure.walk/macroexpand-all)

    (defmacro compiler [tag props & children]
      `(.createElement react ~(name tag) ~props ~@children))

    (defmacro h [body]
      (compile {:compiler `compiler
               :body body})))

  (xpand '(h [:txt "Hello"]))
  (xpand '(h [:vw [:txt "Hello"]]))
  (xpand '(h [:vw [:txt "Hello"] [:txt "World"]]))
  (xpand '(h [:vw [:vw [:txt "hello"]]]))
  (xpand '(h [:vw (if bool [:txt "Yes"] [:txt "No"])]))
  (xpand '(h [custom "Hello"])))

