(ns demo.router
  (:require [rewrap.interop :as interop]
            [helix.impl.props :as hxp]
            [demo.core]
            #?@(:cljs [["react-router-dom" :as react-router-dom]]))
  #?(:cljs (:require-macros [demo.router])))

#?(:cljs
   (def rr react-router-dom))

#?(:clj
   (interop/intern-comps {:emitter 'demo.core/emitter
                          :parser {:tag   #(interop/js-module* `rr (str (name %)))
                                   :props #(hxp/-native-props %)}
                          :interns '[BrowserRouter
                                     Routes
                                     Route
                                     Outlet
                                     Navigate
                                     Link]}))
