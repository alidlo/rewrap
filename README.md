# Rewrap (WIP)

Rewrap makes it easy to use React libraries in Clojurescript without all the boilerplate.

Preview:

```clj
;; intern the components in my-app/comps.cljc 
(interop/intern-comps {:js-ns   'react-native
                      :interns  [View Text] 
                      :compiler 'my-app.ui/compiler}))

;; use them in another namespace, requiring [my-app.comps :as c]
(c/view (c/text "Hello!"))
```

## Usage 

Currently, Rewrap exposes a single function, `intern-comps` that creates component wrapper definitions in a namespace.

Rewrap works with existing Clojurescript React wrappers, such as [Helix](https://github.com/Lokeh/helix).

Below, for example, is how you could use Helix and React Native together:

```clj
;; my-app/ui.cljc
(ns my-app.ui
  (:require [helix.core]
            [rewrap.interop :as interop]
            #?@(:cljs [["react-native" :as react-native]]))
  #?(:cljs (:require-macros [my-app.ui])))

#?(:cljs
   (def rn react-native))

#?(:clj
   (interop/intern-comps {:js-ns   'my-app.ui/rn
                          :interns [View Text TextInput Button] 
                          :compiler 'helix.core/$}))
```

Notice how as the `:js-ns` we pass `my-app.ui/rn` so the list of `interns` will be accessed under that module (i.e. rn/View, rn/Text, etc.).

As the compiler, we passed the `helix.core/$` macro, so the components props and children will be pre-compiled according to that macro.

Do note, that in order to pre-compile these components arguments as well, they're also interned as macros. 

Make sure to define `rewrap.interop` in `:cljs` and `:clj` namespaces so the appropriate code can be required.

By default, the components are interned in lisp-case. So `TextInput`, for example, would be accessed as `text-input`. You can change this with the `parse-name` option. 

For extra processing of props before passing them to the macro, you can pass the `parse-props` option.




