# Rewrap (alpha)

The primary motivation for Rewrap is two-fold: 

* Expose standalone utilities for pre-compiling React components.
* Wrap entire React libraries with one macro call.

These two facilities are in [rewrap](https://github.com/alidlo/rewrap/tree/master/rewrap).

```clj
;; # Rewrap Example

;; intern entire React Native library in myapp/ui.core.cljc
;; require [rewrap.interop as interop]
(interop/intern-comps 
  {:emitter 'my-app/emit-element
   :parser {:tag   #(interop/js-module* `react-native (camel-case (str (name %))))
            :props #(impl/native-props %)}})

;; use interned components
;; require [myapp.ui.core :as c]
(c/view (c/text "Rewrap!"))
```

Along with that, is a [rewrap/hiccup](https://github.com/alidlo/rewrap/tree/master/rewrap-hiccup), a compiler for composing components as Clojure data. There's existing libraries for compiling  Hiccup-like syntax, but they pre-configured their parsing of component arguments and weren't extensible enough for mixing built-in and interned components. 

```clj
;; # Rewrap Hiccup Example

;; configure hiccup compiler 
;; require [rewrap.hiccup as hiccup]
(defmacro h "Component hiccup compiler."
  [body]
  (hiccup/compile body
                  {:emitter  'my-app/emit-element
                   :parsers  {:<>      {:tag 'react/Fragment}
                              keyword? {:tag   #(interop/js-module* `react-native (camel-case (str (name %))))
                                        :props impl/-native-props})})}
;; use hiccup templates
(h [:view [:text "Rewrap!"]])
```

Finally, to write React hooks in idiomatic Clojure and have a better development experience (e.g. hook warnings and fast refresh), we recommend using Rewrap with a minimal React wrapper like [Helix](https://github.com/Lokeh/helix).

## Install 

As this project is still in alpha please use this repo as git dependency in tools.deps.

## Usage 

See the respective repo's README for documentation.

## Demo 

The [demo](https://github.com/alidlo/rewrap/tree/master/demo) shows how you can combine Rewrap and Helix in a [React Native Web](https://github.com/necolas/react-native-web) project.

To run: 
```
yarn install 
shadow-cljs watch demo
```

## License

EPL
