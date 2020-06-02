(ns rewrap.interop
  (:require #?@(:cljs [[goog.object]])
            #?@(:clj [[clojure.string :as string]
                      [rewrap.compile.component :as component]]))
  #?(:cljs (:require-macros [rewrap.interop])))

;; ## casing utilities
#?(:clj
   (defn camel->lisp "Convert camel-case string `s` to lisp-case."
     [s]
     (-> s
         (string/replace #"(.)([A-Z][a-z]+)" "$1-$2")
         (string/replace #"([a-z0-9])([A-Z])" "$1-$2")
         (string/lower-case))))

;; ## wrapping utilities
#?(:clj
   (defn js-module*
     "Wrapper to get js module from given object `o` and key `k`, throws error if module does not exist."
     [o k]
     `(if-let [module# (goog.object/get ~o ~k)]
        module#
        (throw (ex-info "Interned component not found" {:obj ~o :key ~k})))))

;; ## interning utilities
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

     (defn- comp-factory*
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
         :or   {parse-name     camel->lisp
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
