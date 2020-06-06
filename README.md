# Rewrap (WIP)

Wrap React in Clojurescript, without the boilerplate.

The primary motivation for Rewrap is to remove the boilerplate in wrapping entire React libraries.

The intention was to pre-compile these components with existing React wrappers, such as [Helix](https://github.com/Lokeh/helix), which has great support for React hooks development, and [Hicada](https://github.com/rauhs/hicada), which allows you to pre-compile as Hiccup data. While using Rewrap with these libraries is possible, mixing them and composing both built-in and custom components was a bit error-prone and inflexible, so also included is a more extensible Hiccup compiler, so you can compose React components in idiomatic Clojure.

The [example](https://github.com/alidlo/rewrap/tree/master/example) in this repo shows how you can combine Rewrap and Helix in a react native web project.

# Usage 

## Install

![Clojars Project](https://img.shields.io/clojars/v/rewrap)

Install latest version from clojars.

Make sure React is installed as an npm dependency as well.

## Compose components

To compile Hiccup data into React elements, you can use `rewrap.hiccup/compile`.

Compile accepts a custom `emitter` macro, with `[tag props children]` as arguments and should return them in a React create element call.

Along with that, you can pass a custom `:parsers` map, of `{clause parser}` pairs. Each parser will be called if the clause matches its respective tag. A clause can either be a value (i.e. `:<>`) or predicate function (i.e. `keyword?`). A parser can either be a function that receives and returns `[tag props children]` or a map with one of keys `:tag`, `:props`, or `:children` for parsing those respective values.

```cljs
;; 1) declare custom compiler
(defmacro emitter "Wraps component args in react create element call."
  [tag props children]
  `(create-element ~tag ~props ~@children))

(defmacro h "Component hiccup compiler."
  [body]
  (hiccup/compile body
                  {:emitter      'example/emitter
                   :parsers      {:<>      {:tag 'react/Fragment}
                                  keyword? {:tag   #(interop/js-module* `react-native (camel-case (str (name %))))
                                            :props impl/-native-props})})}
;; 2) use hiccup templates
(h [:view [:text "Hello!"]])
```

*Note: the parsers are called in the order they are found, so as a precaution, you should pass an `array-map` to parsers. Though this is already done under the hood by Clojure if map size is approximately 8.*

## Wrap components

### Intern component libraries

To wrap entire React libraries you can use `rewrap.interop/intern-comps`.

It accepts a custom `:emitter` macro (same usage as `:emitter` in `hiccup/compile`).

Along with that you can pass one custom `:parser` (same usage as the `:parsers` in `hiccup/compile`).

The interned component are defined as macro definitions, since that makes it possible to pre-compile their arguments,  and their names are lisp-cased, e.g. `c/TextInput` would be accessed as `c/text-input`.

```clj
(interop/intern-comps {:emitter 'example/emitter
                       :parser {:tag   #(interop/js-module* `rr (str (name %)))
                                :props #(impl/-native-props %)}})
```

### Parse component arguments

#### Tag 

To parse a component tag as a javascript module you can use `rewrap.interop/js-module*`.

It accepts the object's namespace and the given access key, i.e. ```(js-module* `rn "View")```.


### Props

To parse props, we're currently just using [Helix](https://github.com/Lokeh/helix) (see example for details).

Using Helix's prop implementation isn't ideal, but we're trying to leverage existing wrappers where possible, especially since mixing two different parsers is error-prone.

### Children

If you plan to compose components as Hiccup data, we recommend you don't intern a component's children with any special parsing as that may conflict with Hiccup compilation.


# License

EPL, same as Clojure.
