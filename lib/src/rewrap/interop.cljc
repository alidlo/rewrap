(ns rewrap.interop
  (:require #?@(:cljs [[goog.object]])
            #?@(:clj [[rewrap.compile.component :as component]
                      [rewrap.util.casing :as casing]]))
  #?(:cljs (:require-macros [rewrap.interop])))

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

     (defn js-module* "Macro helper for getting js module, throws error if module does not exist."
       [sym k]
       `(if-let [module# (goog.object/get ~sym ~k)]
          module#
          (throw (ex-info "Interned component not found" {:sym ~sym :key ~k}))))

     (defn comp-factory*
       "Macro helper for wrapping component in `compiler` macro that parsers cljs component `args`."
       [sym k {:keys [compiler parse-name parse-args]}]
       `(defmacro ~(symbol (parse-name k))
          [& args#]
          `(~~compiler ~(js-module* ~sym ~k) ~@(~parse-args args#))))

     (defmacro intern-comps
       "Create component defs from from given `js-ns` and `interns`, using custom `compiler` macro. 
        Note: The `interns` can be quoted to avoid linter 'unresolved symbol' error, i.e. '[View Text].
        Can optionally pass additional options:
         - `parse-name`, fn to parse interned component name.
         - `parse-props`, fn to pre-process props passed to compiler.}"
       [{:keys [js-ns interns compiler parse-name parse-props parse-child]
         :or   {parse-name     casing/camel->lisp
                parse-props    identity
                parse-child    identity}}]
       `(do
          ~@(for [k (symsolve (as-is interns))]
              (comp-factory* js-ns
                             (str k)
                             {:compiler    compiler
                              :parse-name  parse-name
                              :parse-args `(fn [args#]
                                             (component/parse-args args# {:parse-props ~parse-props
                                                                          :parse-child ~parse-child}))}))))))


(comment
  (macroexpand '(intern-comps  'rn '[View])))
