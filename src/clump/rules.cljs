(ns clump.rules)

(defn distinct-traits [cards trait]
  "Returns a lazy sequence of the distinct values of the trait among the cards."
  (->> cards
       (map trait)
       (remove nil?)
       (distinct)))

(defn clump? [traits cards]
  "Whether the cards form a valid clump: For every trait, all the cards must
  have the same value or they must all have a different value."
  (let [trait-counter (comp (map (partial distinct-traits cards))
                            (map count))
        trait-counts  (into [] trait-counter traits)]
    (when (every? #{1 (count cards)} trait-counts)
      cards)))
