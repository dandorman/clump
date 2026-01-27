(ns clump.core
  (:require [clump.game :as game]
            [clump.ui.utils :refer [alert class-names]]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce game-state (r/atom {}))

(defonce game-history (r/atom []))

(add-watch game-state :history
           (fn [_ _ _ n]
             (when-not (= (last @game-history) n)
               (swap! game-history conj n))))

(defn new-game! []
  (reset! game-history [])
  (swap! game-state merge (game/new-game))
  game-state)

(defn undo! []
  (when (> (count @game-history) 1)
    (swap! game-history pop)
    (reset! game-state (last @game-history))))

(defn hint! []
  (let [clump (game/hint @game-state)]
    (assert clump "There must be a clump.")
    (swap! game-state #(-> %
                           (assoc :hint (set clump))
                           (assoc :selected #{})))))

(defn card-selected! [card]
  (swap! game-state #(-> (game/card-selected % card)
                         (update :board (fn [board] (map (fn [c] (dissoc c :highlight)) board)))))
  (when (game/over? @game-state)
    (alert "We're done here.")))

(defn shape-wrapper [contents]
  [:svg {:view-box "0 0 100 100"}
   contents])

(defmulti shape :shape)

(defmethod shape :circle [{:keys [color fill]}]
  [shape-wrapper
   [:circle {:class (class-names [color fill])
             :cx 50 :cy 50 :r 45}]])

(defmethod shape :square [{:keys [color fill]}]
  [shape-wrapper
   [:rect {:class (class-names [color fill])
           :x 5 :y 5 :width 90 :height 90}]])

(defmethod shape :triangle [{:keys [color fill]}]
  [shape-wrapper
   [:polygon {:class (class-names [color fill])
              :points "10 85, 50 16, 90 85"}]])

(defmethod shape :blank [_]
  [shape-wrapper nil])

(defn face [side traits]
  (into [:div {:class (str "face " (name side))}]
        (repeatedly (:number traits) #(shape traits))))

(defn card [{:keys [traits selected drawn]}]
  (let [faces (map (fn [[s t]] [face s t])
                   (zipmap [:front :back]
                           (remove nil? [(drawn traits) traits])))]
    [:div.card-container
     (into [:div {:class (class-names {"card"      true
                                       "blank"     (= :blank (:color traits))
                                       "selected"  (selected traits)
                                       "highlight" ((:hint @game-state) traits)
                                       "flip"      (drawn traits)})
                  :on-click #(card-selected! traits)}]
           faces)]))

(defn board []
  (let [{:keys [board selected drawn]} @game-state]
    (into [:div.board]
          (for [idx (range (count board))
                :let [traits (nth board idx)]]
            ^{:key idx}
            [card {:traits traits
                   :selected selected
                   :drawn drawn}]))))

(defn game-ui []
  [:div.game
   [:div.controls
    [:p (str "Score: " (:score @game-state))]
    [:button {:on-click hint!} "Hint"]
    [:button {:on-click undo!} "Undo"]
    [:button {:on-click new-game!} "New Game"]]
   [board]])

(defn init []
  (when (empty? @game-state)
    (new-game!))
  (rdom/render [game-ui]
               (.getElementById js/document "app")))
