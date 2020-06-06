# Rewrap (WIP)

Wrap React in idiomatic Clojurescript, without the boilerplate.

![Clojars Project](https://img.shields.io/clojars/v/rewrap)

The primary motivation for Rewrap is to remove the boilerplate in wrappings entire React libraries.

The intention was to pre-compile these components with existing React wrappers, such as [Helix](https://github.com/Lokeh/helix), which has great support for React hooks development, and [Hicada](https://github.com/rauhs/hicada), which allows you to compose component as Hiccup data. While that is possible, mixing these two libraries and composing both built-in and custom components proved error-prone and inflexible, so also included is a more extensible Hiccup compiler.

The [example](https://github.com/alidlo/rewrap/tree/master/example) in this repo shows how you can combine Rewrap and Helix in a React Native project.

## Usage 

### Compose components

To compile Hiccup data into React elements, you can use `rewrap.hiccup/compile`.

Compile accepts a custom `emitter` macro, with `[tag props children]` as arguments and should return them in a React create element call. 

Along with that, you can pass a custom `:parsers` map, of `{clause parser}` pairs. Each parser will be called if the clause matches its respective tag. A clause can either be a value (i.e. `:<>`) or predicate function (i.e. `keyword?`). A parser can either be a function that transforms [tag props children] or a map with one of `:tag`, `:props`, or `:children` keys for parsing those respective values.

```cljs
;; 1) declare custom compiler
(defmacro emitter "Wraps component args in react create element call."
  [tag props children]
  `(helix.core/create-element ~tag ~props ~@children))

(defmacro h "Component hiccup compiler."
  [body]
  (hiccup/compile body
                  {:emitter      'example/emitter
                   :parsers      {:<>      {:tag 'helix.core/Fragment}
                                  keyword? {:tag   #(interop/js-module* `react-native (camel-case (str (name %))))
                                            :props helix.core/-native-props})})}
;; 2) use hiccup templates
(h [:view [:text "Hello!"]])
```

*Note: the parsers are called in the order they are found, so as a precaution, you should pass an `array-map` to parsers. Though this is already done under the hood by Clojure if map size is approximately 8.

### Wrap libraries

#### intern components
To wrap entire React libraries you can use `rewrap.interop/intern-comps`.

It accepts a custom `:emitter` macro (same usage as `:emitter` in `hiccup/compile`).

Along with that you can pass one custom `:parser` (same usage as the `:parsers` in `hiccup/compile`).

```clj
(interop/intern-comps {:emitter 'example/emitter
                       :parser {:tag   #(interop/js-module* `rr (str (name %)))
                                :props #(hxp/-native-props %)}})
```
