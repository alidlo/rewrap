# Rewrap 

## Usage 

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
