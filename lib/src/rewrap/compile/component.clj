(ns rewrap.compile.component)

(defn has-props?
  "Checks if component `args` has props.
  Props are assumed to be a static cljs map. A symbol is considered to be a dynamic child."
  [args] (map? (first args)))

(defn normalize-args "Converts `args` to [props children] tuple."
  [args] 
  (let [props    (if (has-props? args) (first args) nil)
        children (if props (rest args) args)]
    [props children]))

(defn parse-args 
  "Parse component `args` list, accepts `parse-props` and `parse-child` options."
  [args {:keys [parse-props parse-child]
         :or {parse-props identity
              parse-child identity}}]
  (let [[props children] (normalize-args args)]
    (cons (when props (parse-props props))
          (mapv parse-child children))))

(comment
  (defn args [x] (parse-args x {:parse-props (fn [props] `(ex/->js ~props))
                                :parse-child (fn [child] `(ex/hiccup ~child))}))
  
  (args (list {:style []}))
  (args (list {:style []} "Hello" "World")))
