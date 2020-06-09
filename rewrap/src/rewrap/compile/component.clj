(ns rewrap.compile.component)

;; TODO what about JS maps?
(defn props?
  "Checks if value `x` is component props.
  Props are assumed to be a static cljs map. A symbol is considered to be a dynamic child."
  [x] (map? x))

(defn normalize-args "Converts `args` to [tag props children] vector."
  [args]
  (let [tag      (first args)
        props    (if (props? (second args)) (second args) nil)
        children (into [] (drop (if props 2 1) args))]
    [tag props children]))

(defn- apply-map-parser "Apply `:tag` `:props` and `:children` parser fns on normalized args."
  [[t p ch] {:keys [tag props children]
             :or {tag identity
                  props identity
                  children identity}}]
  (letfn [(fval [fv x] (if (fn? fv) (fv x) fv))]
   [(fval tag t) (when p (fval props p)) (fval children ch)]))

(defn- apply-parser 
  "Apply `parser` on normalized args.
   Parser can be a function (fn [nargs] ,,,) or map with individual :tag, :props, :children options."
  [nargs parser]
  (if (fn? parser) (apply parser nargs) (apply-map-parser nargs parser)))

(defn- check-clause 
  "Check whether given `clause` applies to `tag`. 
   Clause can be a predicate fn (e.g. symbol?) or primitive key (e.g. :<>) to match."
  [tag clause]
  (if (fn? clause) (clause tag) (= clause tag)))

(defn parse-args
  "Parse component `args` using `parsers`.
   Parsers are a {clause parser} as defined by #check-clause and #apply-parser, respectively.
   If a parser returns anything other than a vector, parsing is terminated early."
  [args {:keys [parsers] :or {parsers {}}}]
  (let [nargs (normalize-args args)]
    (reduce (fn [acc [clause parser]]
              (if (vector? acc)
                (if (check-clause (first acc) clause)
                  (apply-parser acc parser)
                  acc)
                (reduced acc)))
            nargs
            parsers)))

(comment
  (defn args [x] (parse-args x {:parsers (array-map
                                          :<>      {:tag 'react/Fragment}
                                          keyword? {:tag (fn [tag] `(ex/module* ~tag))}
                                          any?     {:props    (fn [m]  `(ex/props* ~m))
                                                    :children (fn [ch] `(ex/hiccup* ~ch))})}))

  (args [:<> 'child])
  (args [:vw {:style []}])
  (args [:vw {:style []} "Hello" "World"]))
