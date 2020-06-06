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

     (defn comp-factory* "Macro helper for wrapping component of key `k` in `emitter` macro and args `parsers`."
       [k {:keys [emitter parsers]}]
       `(defmacro ~(symbol (camel->lisp k))
          [& args#]
          `(~~emitter ~@(component/parse-args (cons ~k args#) {:parsers ~parsers}))))
     
     (defmacro intern-comps
       "Create component defs for given `interns` using custom `emitter` macro and component args `parsers`. 
        Note: The `interns` can be quoted to avoid linter 'unresolved symbol' error, i.e. '[View Text]."
       [{:keys [interns emitter parser]
         :or {parser {}}}]
       `(do
          ~@(for [k (symsolve (as-is interns))]
              (comp-factory* (str k) {:emitter emitter :parsers {any? parser}}))))))


(comment
  (require '[clojure.walk])
  (do
    (def xpand clojure.walk/macroexpand-all)
    (def parsers {any? {:tag (fn [tag] (js-module* `rr tag))}}))

  (macroexpand '(intern-comps  {:parsers {}
                                :interns '[View]})))

