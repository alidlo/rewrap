# Rewrap Hiccup

## Usage 

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
