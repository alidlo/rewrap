(ns my-app.ui
  (:require [helix.core]
            [rewrap.interop :as interop]
            #?@(:cljs [["react-native" :as react-native]]))
  #?(:cljs (:require-macros [my-app.ui])))

#?(:cljs
   (def rn react-native))

#?(:clj
   (do
     (defmacro compiler [tag & args] `(helix.core/$ ^:native ~tag ~@args))

     (interop/intern-comps {:js-ns    `rr
                            :interns   '[View 
                                         Text
                                         TextInput]
                            :compiler `compiler})))
