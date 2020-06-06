(ns rewrap.hiccup
  (:refer-clojure :exclude [compile])
  (:require [rewrap.compile.component :as component]
            [rewrap.compile.body :as body]))

(declare compile)

(defn create-parsers "Merge custom `parsers` with default one for compiling hiccup children." 
  [parsers opts]
  (merge parsers
        ;; note: we use custom inline fn as key so as to not override any user defined parsers
         {#(identity %) {:children (fn [ch] (mapv #(compile % opts) ch))}}))

(defn compile
  "Compile any hiccup in component `body` using custom `emitter` and `parsers` options.
   Emitter should be a macro that accepts [tag props children] arguments.
   If given component is already compiled, pass `precompiled?` (fn [tag] bool) and only hiccup children are compiled.
   Parsers is a {clause parser} map as specified in #component/parse-args."
  [body {:keys [precompiled? emitter parsers]
         :or {precompiled? (fn [_] false)}
         :as opts}]
  (cond
    (vector? body) (let [[tag props children] (component/normalize-args body)]
                     (if-not (precompiled? tag)
                       `(~emitter ~@(component/parse-args body {:parsers (create-parsers parsers opts)}))
                       ;; note: precompiled component children are spliced because they are spread in their macro body
                       `(~tag ~props ~@(mapv #(compile % opts) children))))
    (list? body)   (body/parse-expr-output body #(compile % opts))
    :else body))


(comment
  (require '[clojure.walk])
  (do
    (def xpand clojure.walk/macroexpand-all)

    (defmacro emitter [tag props children]
      `(.createElement react ~(name tag) (ex/->props ~props) ~@children))

    (defmacro h [body]
      (compile body {:emitter `emitter}))

    (defmacro component [props & children]
      `(emitter "custom" ~props ~@children)))

  ;; keyword vectors 
  (xpand '(h [:txt "Hello"]))
  (xpand '(h [:vw [:txt "Hello"]]))
  (xpand '(h [:vw [:txt "Hello"] [:txt "World"]]))
  (xpand '(h [:vw [:vw [:txt "hello"]]]))

  ;; fn lists
  (xpand '(h (let [msg "hello"] [:txt msg])))
  (xpand '(h [:vw (if bool [:txt "Yes"] [:txt "No"])]))
  (xpand '(h (let [msg "hello"] (when msg [:txt msg]))))
  
  ;; custom components
  (xpand `(h [component [:txt "hello"]]))
  (xpand `(h [component {:style []} [:txt "hello"]]))
  (xpand `(h [component (c/txt "hello")])))

