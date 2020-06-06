(ns demo.core
  (:require [rewrap.interop :as interop]
            [helix.core]
            [helix.impl.props :as hxp]
            [clojure.string :as string]
            #?@(:cljs [[goog.object]
                       ["react-native" :as react-native]])
            #?@(:clj [[rewrap.hiccup :as hiccup]]))
  #?(:cljs (:require-macros [demo.core])))

#?(:cljs
   (def rn react-native))

#?(:clj
   (do
     (defmacro emitter "Wraps component args in react create element call."
       [tag props children]
       `(helix.core/create-element ~tag ~props ~@children))

     (defmacro h "Component hiccup compiler."
       [body]
       (hiccup/compile body
                       {:precompiled? #(and (symbol? %) (contains? #{"hx" "rtr"} (namespace %)))
                        :emitter      'demo.core/emitter
                        :parsers       (array-map
                                        :<>      {:tag 'helix.core/Fragment}
                                        keyword? {:tag   #(interop/js-module* `rn (string/capitalize (str (name %))))
                                                  :props hxp/-native-props}
                                        symbol?  {:props hxp/-props})}))

     (def default-defnc-opts "Helix feature flags to enable by default in `defnc` component."
       {:helix/features {:fast-refresh true}})

     (defmacro defnc
       "Create React component whose body will be pre-compiled."
       [type & form-body]
       (let [docstr    (when (string? (first form-body))
                         (first form-body))
             params     (if (nil? docstr)
                          (first form-body)
                          (second form-body))
             body      (if (nil? docstr)
                         (rest form-body)
                         (rest (rest form-body)))
             opts?     (map? (first body))
             opts      (if opts?
                         (merge default-defnc-opts (first body))
                         default-defnc-opts)
             body   (if opts? (rest body) body)]
         `(helix.core/defnc ~type ~params ~opts (h [:<> ~@body]))))))

