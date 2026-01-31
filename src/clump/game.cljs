(ns clump.game
  (:require [clojure.set :as set]
            [clump.utils :refer [combinations map-combinations]]
            [clump.rules :refer [clump?]]))

(def traits {:shape  [:circle :square :triangle]
             :color  [:red :green :blue]
             :fill   [:empty :striped :solid]
             :number [1 2 3]})

(defn clumps [cards]
  (filter (partial clump? (keys traits)) (combinations (filter #(not= :blank (:shape %)) cards) 3)))

(def blank {:shape :blank, :color :blank, :fill :blank, :number 1})

(def deck (map-combinations traits))

(defn board-position [board card]
  (count (take-while #(not= card %) board)))

(defn draw [game]
  (cond
    (< 3 (count (:deck game)))
    (let [shuffled (shuffle (:deck game))
          next-three (take 3 shuffled)
          future-board (-> (:board game)
                           set
                           (set/difference (set (:selected game)))
                           (concat next-three))]
      (if (seq (clumps future-board))
        (assoc game :draw (vec next-three)
                    :deck (vec (drop 3 shuffled)))
        (recur game)))

    (= 3 (count (:deck game)))
    (assoc game :draw (vec (take 3 (:deck game)))
                :deck (vec (drop 3 (:deck game))))

    ; the deck is empty, fill space with "blank" cards
    :else
    (assoc game :draw (vec (repeatedly 3 (constantly blank))))))

(defn new-game
  ([]
   (new-game deck))
  ([deck]
   (let [shuffled (shuffle deck)
         board (take 12 shuffled)
         deck (drop 12 shuffled)]
     (if (seq (clumps board))
       {:score 0
        :deck (vec deck)
        :board (vec board)
        :selected #{}
        :hint #{}
        :hint-remaining #{}
        :drawn {}}
       (recur deck)))))

(defn hint [{:keys [board hint hint-remaining selected] :as game}]
  (cond
    ; reveal the next card from the current hint
    (seq hint-remaining)
    (let [next-hint (-> hint-remaining shuffle first)]
      (-> game
          (update :hint conj next-hint)
          (update :hint-remaining disj next-hint)
          (assoc :selected (set/intersection hint selected))))

    ; revealed entire hint; clear hint
    (seq hint)
    (-> game
        (assoc :hint #{})
        (assoc :hint-remaining #{}))

    ; pick a new hint
    :else
    (let [[hint & hint-remaining] (-> board clumps shuffle first shuffle)]
      (-> game
          (assoc :hint #{hint})
          (assoc :hint-remaining (into #{} hint-remaining))
          (assoc :selected #{})))))

(defn toggle-card [game card]
  (let [selected (:selected game)]
    (assoc game :selected (if (selected card)
                            (disj selected card)
                            (conj selected card)))))

(defn replace-clump [game clump]
  (let [game (draw game)
        old->new (zipmap (sort-by (partial board-position (:board game)) clump)
                         (sort-by #(clump %) (:draw game)))
        new->old (zipmap (vals old->new) (keys old->new))]
    (-> game
        (dissoc :draw)
        (assoc :selected #{}
               :drawn new->old
               :board (vec (take 12 (map #(get old->new % %) (:board game))))))))

(defn card-selected [game card]
  (let [game (assoc (toggle-card game card) :drawn {})
        selected (:selected game)
        game (cond-> game (not ((:hint game) card)) (assoc :hint #{}))]
    (if (= 3 (count selected))
      (let [score (:score game)]
        (if (clump? (keys traits) selected)
          (-> game
              (replace-clump selected)
              (assoc :hint #{})
              (assoc :hint-remaining #{})
              (assoc :score (inc score)))
          (assoc game :score (dec score) :selected #{})))
      game)))

(defn over? [game]
  (and (empty? (:deck game))
       (empty? (clumps (:board game)))))
