# Rewrap (WIP)

Rewrap makes it easy to wrap React libraries.

Example:

```clj
(ns my-app.ui
  (:require [helix.core]
            [rewrap.interop :as interop]
            #?@(:cljs [["react-native" :as react-native]]))
  #?(:cljs (:require-macros [my-app.ui])))

#?(:cljs
   (def rn react-native))

#?(:clj
   (interop/intern-comps {:js-ns   `rn
                          :interns [View
                                    Text] 
                          :compiler 'helix.core/$}))
```
