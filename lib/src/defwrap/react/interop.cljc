(ns defwrap.react.interop
  (:require [helix.core]
            #?@(:cljs [[goog.object]])
            #?@(:clj [[clojure.string :as string]]))
  #?(:cljs (:require-macros [reso.ui.interop])))

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

     (defn- transform-args-props "Apply function `f` to `args` props."
       [args f]
       (if-let [props (when (map? (first args))
                        (first args))]
         (assoc (into [] args) 0 (f props))
         args))

     (defn- js-module*
       "Macro helper for getting js module and warning if it does not exist."
       [sym k]
       `(if (goog.object/get ~sym ~k)
          (goog.object/get ~sym ~k)
          (throw (ex-info "Intered component not found" {:sym ~sym
                                                         :key ~k}))))

     (defn- cljs-comp*
       "Macro helper for wrapping component in [helix.core/$] macro that pre-compiles cljs props."
       [sym k parse-args]
       `(defmacro ~(symbol (camel->lisp k))
          [& args#]
          `(helix.core/$ ~(js-module* ~sym ~k) ~@(~parse-args args#))))

     (defmacro intern-comps
       "Intern list of a component `tags` for provided `sym`.
        Arguments: 
         - `sym` symbol expected to reference a js import object.
         - `tags` can be a quoted or unquoted list."
       ([sym tags] `(intern-comps ~sym ~tags {}))
       ([sym tags {:keys [parse-props]
                   :or   {parse-props identity}}]
        `(do ~@(for [t (symsolve (as-is tags))]
                 (cljs-comp* sym (str t) `(fn [args#] (transform-args-props args# ~parse-props)))))))))

