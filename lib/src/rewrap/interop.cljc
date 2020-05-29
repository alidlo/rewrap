(ns rewrap.interop
  (:require #?@(:cljs [[goog.object]
                       ["react" :as react]])
            #?@(:clj [[clojure.string :as string]]))
  #?(:cljs (:require-macros [rewrap.interop])))

#?(:cljs 
    (def create-element react/createElement))

;; ## Casing Utilities
#?(:clj
   (defn camel->lisp "Convert camel-case string `s` to lisp-case."
     [s]
     (-> s
         (string/replace #"(.)([A-Z][a-z]+)" "$1-$2")
         (string/replace #"([a-z0-9])([A-Z])" "$1-$2")
         (string/lower-case))))


;; ## Component Wrapper Utilities
#?(:clj
   (do
     (defn- as-is "Ensures value is not quoted. (It's useful to quote args to prevent unresolved symbol errors.)"
       [x]
       (if (and (seq? x) (= (first x) 'quote)) (second x) x))

     (defn- symsolve "Ensures value is resolved if it is a symbol, otherwise returns as is."
       [x]
       (if (symbol? x)
         (let [val (deref (resolve x))]
           (if val val (throw (ex-info "Could not resolve symbol" {:sym x}))))
         x))

     (defn transform-args-props "Apply function `f` to `args` props."
       [args f]
       (if-let [props (when (map? (first args))
                        (first args))]
         (assoc (into [] args) 0 (f props))
         args))

     (defn js-module* "Macro helper for getting js module, throws error if module does not exist."
       [sym k]
       `(if (goog.object/get ~sym ~k)
          (goog.object/get ~sym ~k)
          (throw (ex-info "Interned component not found" {:sym ~sym :key ~k}))))
     
     (defmacro compile-element "Default compile element macro."
       [tag & args]
       `(create-element ~tag ~@args))

     (defn comp-factory*
       "Macro helper for wrapping component in `compiler` macro that parsers cljs component `args`."
       [sym k {:keys [compiler parse-name parse-args]}]
       `(defmacro ~(symbol (parse-name k))
          [& args#]
          `(~'~compiler ~(js-module* ~sym ~k) ~@(~parse-args args#))))


     (defmacro intern-comps
       "Intern list of a component `tags` for provided `sym` based on given `opts`.
        Arguments: 
         - `sym` symbol expected to reference a js import object.
         - `tags` can be a quoted or unquoted list.
         - opts "
       ([sym tags] `(intern-comps ~sym ~tags {}))
       ([sym tags {:as _opts
                   :keys [compiler
                          parse-name
                          parse-props]
                   :or   {compiler   `compile-element
                          parse-name   camel->lisp
                          parse-props  identity}}]
        `(do 
           ~@(for [t (symsolve (as-is tags))]
                 (comp-factory* sym (str t) {:compiler    compiler
                                             :parse-name  parse-name
                                             :parse-args `(fn [args#] (transform-args-props args# ~parse-props))})))))))


(comment
  (macroexpand '(intern-comps  'rn '[View]))
  )
