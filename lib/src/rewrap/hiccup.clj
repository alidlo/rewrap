(ns rewrap.hiccup
  (:refer-clojure :exclude [compile])
  (:require [rewrap.compile.component :as component]
            [rewrap.compile.body :as body]))

(defn compile
  "Compile any hiccup in component `body` using custom `compiler`.
   Compiler macro should accept [tag props & children]. Children are destructured as that's how they appear in body."
  ([body opts] (compile (assoc opts :body body)))
  ([{:keys [body compiler]
     :as opts}]
   (cond
     (vector? body) `(~compiler ~(first body)
                                ~@(component/parse-args
                                   (rest body)
                                   {:parse-child (fn [ch] (compile ch opts))}))
     (list? body)   (body/parse-expr-output body #(compile % opts))
     :else body)))


(comment
  (require '[clojure.walk])
  (do
    (def xpand clojure.walk/macroexpand-all)

    (defmacro compiler [tag props & children]
      `(.createElement react ~(name tag) (ex/->props ~props) ~@children))

    (defmacro h [body]
      (compile {:compiler `compiler
                :body body}))

    (defmacro component [props & children]
      `(compiler "custom" ~props ~@children)))

  ;; keyword vectors 
  (xpand '(h [:txt "Hello"]))
  (xpand '(h [:vw [:txt "Hello"]]))
  (xpand '(h [:vw [:txt "Hello"] [:txt "World"]]))
  (xpand '(h [:vw [:vw [:txt "hello"]]]))

  ;; fn lists
  (xpand '(h (let [msg "hello"] [:txt msg])))
  (xpand '(h [:vw (if bool [:txt "Yes"] [:txt "No"])]))

  ;; custom components
  (xpand `(h [component [:txt "hello"]]))
  (xpand `(h [component {:style []} [:txt "hello"]]))
  (xpand `(h [component (c/txt "hello")])))

