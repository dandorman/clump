(ns clump.core
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [clump.game :as game]
            [clump.ui.utils :refer [alert class-names]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [chan put! <!]]))

(enable-console-print!)

(def game-state (atom {}))

(def game-history (atom []))
(add-watch game-state :history
           (fn [_ _ _ n]
             (when-not (= (last @game-history) n)
               (swap! game-history conj n))))

(defn new-game! []
  (swap! game-history empty)
  (swap! game-state #(merge % (game/new-game)))
  game-state)

(defn undo! []
  (when (> (count @game-history) 1)
    (swap! game-history pop)
    (reset! game-state (last @game-history))))

(defn no-clumps! [game]
  (if-let [clump (first (game/clumps (:board @game)))]
    (do
      (om/transact! game :score dec)
      (alert "Sorry, but there IS a clump."))
    (let [drawn (game/draw @game)]
      (om/update! game (assoc (dissoc drawn :draw) :board (concat (:board drawn) (:draw drawn)))))))

(defn hint! [g]
  (if-let [clump (game/hint @g)]
    (om/transact! g :board (fn [_] (map #(if ((set clump) %) (assoc % :highlight true) %) (:board @g))))
    (alert "No clumps?")))

(defn shape-wrapper [contents]
  (dom/svg #js {:viewBox "0 0 100 100"}
           contents))

(defmulti shape :shape)

(defmethod shape :circle [{:keys [color fill]}]
  (shape-wrapper (dom/circle #js {:className (class-names [color fill])
                                  :cx 50 :cy 50 :r 45})))

(defmethod shape :square [{:keys [color fill]}]
  (shape-wrapper (dom/rect #js {:className (class-names [color fill])
                                :x 5 :y 5 :width 90 :height 90})))

(defmethod shape :triangle [{:keys [color fill]}]
  (shape-wrapper (dom/polygon #js {:className (class-names [color fill])
                                   :points "10 85, 50 16, 90 85"})))

(defmethod shape :blank [_]
  (shape-wrapper nil))

(defn face [side traits]
  (apply dom/div #js {:className (str "face " (name side))}
         (repeatedly (:number traits) (partial shape traits))))

(defn card [{:keys [traits selected drawn]} owner opts]
  (reify
    om/IRender
    (render [this]
      (let [faces (map (fn [[s t]] (face s t))
                       (zipmap [:front :back]
                               (remove nil? [(drawn traits) traits])))]
        (dom/div #js {:className "card-container"}
                 (apply dom/div #js {:className (class-names {"card"      true
                                                              "blank"     (= :blank (:color traits))
                                                              "selected"  (selected traits)
                                                              "highlight" (:highlight traits)
                                                              "flip"      (drawn traits)})
                                     :onClick (fn [_] (put! (:card-selected opts) traits))
                                     :onTouchEnd (fn [_] (put! (:card-selected opts) traits))}
                        faces))))))

(defn board [game owner opts]
  (reify
    om/IWillMount
    (will-mount [_]
      (go (loop []
            (let [card (<! (:card-selected opts))]
              (om/update! game (game/card-selected @game card)))
              (when (game/over? @game)
                (alert "We're done here."))
            (recur))))
    om/IRender
    (render [this]
      (apply dom/div #js {:className "board"}
             (om/build-all card (map #(hash-map :traits %
                                                :selected (:selected game)
                                                :drawn (:drawn game))
                                     (:board game))
                           {:opts opts})))))

(defn game-ui [game owner opts]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "game"}
               (dom/div #js {:className "controls"}
                        (dom/p nil (str "Score: " (:score game)))
                        (dom/button #js {:onClick (fn [_] (no-clumps! game))} "Clumpless?")
                        (dom/button #js {:onClick (fn [_] (hint! game))} "Hint")
                        (dom/button #js {:onClick (fn [_] (undo!))} "Undo")
                        (dom/button #js {:onClick (fn [_] (new-game!))} "New Game"))
               (om/build board game {:opts opts})))))

(.initializeTouchEvents js/React true)
(om/root game-ui
         (new-game!)
         {:target (. js/document (getElementById "app"))
          :opts {:card-selected (chan)}})
