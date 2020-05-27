# Rewrap (WIP)

Rewrap makes it easy to wrap React libraries.

Example:

```clj
(ns reso.ui.router
  (:require [helix.core]
            [rewrap.interop :as interop]
            #?@(:cljs [["react-router-dom" :as react-router-dom]]))
  #?(:cljs (:require-macros [reso.ui.router])))

#?(:cljs
   (def rr react-router-dom))

#?(:clj
   (interop/intern-comps `rr
                         '[BrowserRouter
                           Routes
                           Route
                           Outlet
                           Navigate
                           Link]
                         {:compiler 'helix.core/$}))
```
