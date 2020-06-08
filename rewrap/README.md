# Rewrap 

## Usage 

### Intern component libraries

To wrap entire React libraries you can use `rewrap.interop/intern-comps`.

It accepts a custom `:emitter` macro, with `[tag props children]` as arguments and should return them in a React create element call.

Along with that you can pass one custom `:parser` (similar usage to the parsers documented [below](#parse-component-arguments)).

The interned component are defined as macro definitions, since that makes it possible to pre-compile their arguments, and their names are lisp-cased, e.g. `c/TextInput` would be accessed as `c/text-input`.

```clj
;; intern component in cljc file, requiring interned macros 
;; e.g. (:require-macros [myapp.ui.core])
(interop/intern-comps {:emitter 'example/emitter
                       :parser {:tag   #(interop/js-module* `react-native (camel-case (str (name %))))
                                :props #(impl/->props %)}
                       :interns [View 
                                 Text]})

;; to test the interned components,  you can macroexpand them in the repl 
(comment 
  (macroexpand '(my-app.ui/text {:style [{:fontSize 16}]} "Hello!"))
)
```

### Parse component arguments

You can use `rewrap.component` namespace utilities to parse component arguments.

`component/normalize-args` accepts the arguments you'd usually pass to a react/createElement call and returns them as `[tag props children]`. Since this utility is intended for pre-compiling clojurescript, props are expected to be a cljs map, any symbol is treated as a child.

```clj
(let [[tag props children] (component/normalize-args args)] ,,,)
```

`component/parse-args` accepts a components `args` and as an option map accepts custom `:parsers` (see section below for details on parsers).

```clj
(component/parse-args args {:parsers {:<> {:tag 'react/Fragment}}})
```

#### Parsers 

Parsers are expected to be `{clause parser}` pairs. Each parser will be called if the clause matches its respective tag. 

A clause can either be a value (i.e. `:<>`) or predicate function (i.e. `keyword?`). 

A parser can either be a function that receives and returns `[tag props children]` or a map with one of keys `:tag`, `:props`, or `:children` for parsing those respective values.

**Tag**

To parse a component tag as a javascript module you can use `rewrap.interop/js-module*`.

It accepts the object's namespace and the given access key, i.e. ```(js-module* `rn "View")```.

**Props**

To parse props, we're currently just using [Helix](https://github.com/Lokeh/helix) (see example for details).

Using Helix's prop implementation isn't ideal, but we're trying to leverage existing wrappers where possible, especially since mixing two different parsers is error-prone.

**Children**

If you plan to compose components as Hiccup data, we recommend you don't intern a component's children with any special parsing as that may conflict with Hiccup compilation.

# License

EPL, same as Clojure.
