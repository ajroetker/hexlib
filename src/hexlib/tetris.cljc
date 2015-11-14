(ns hexlib.tetris
  (:require [hexlib.core :as hex]))

(defn unit-neighbor [unit direction]
  (-> unit
      (update :pivot hex/odd-r-neighbor direction)
      (update :members (partial map #(hex/odd-r-neighbor % direction)))))

(defn unit-rotation [{:keys [pivot] :as unit} clockwise?]
  (update unit :members (partial map #(hex/odd-r-rotation % pivot clockwise?))))

(defn command->direction [command]
  (get {:w 3 :sw 4 :se 5 :e 0} command))

(defn tetris-neighbor [unit command]
  (case command
    (:w :sw :se :e)
    (unit-neighbor unit (command->direction command))
    (:cw :ccw)
    (unit-rotation unit (= :cw command))))

(defn deadly-command? [{:keys [unit grid] :as board} command]
  (let [neighbor (tetris-neighbor unit command)]
    (some #(= neighbor %) (:previous-positions board))))

(defn locking-command? [{:keys [unit grid] :as board} command]
  (let [rows (count grid)
        cols (count (first grid))
        {:keys [members]} (tetris-neighbor unit command)]
    (some #(or (neg? (first %))
               (neg? (second %))
               (neg? (- cols 1 (first %)))
               (neg? (- rows 1 (second %)))
               (get-in grid (reverse %)))
          members)))

(defn grid-fill [grid hexes]
  (reduce #(assoc-in %1 (reverse %2) true) grid hexes))

(defn new-grid
  ([num-cols num-rows] (new-grid num-cols num-rows []))
  ([num-cols num-rows pre-filled]
   (let [blank-grid (vec (repeat num-rows (vec (repeat num-cols false))))]
     (grid-fill blank-grid pre-filled))))

(defn overlaps-filled? [grid {:keys [members] :as unit}]
  (some #(get-in grid (reverse %)) members))

(defn filled-rows [grid]
  (filter #(every? true? %) grid))

(defn num-filled-rows [grid]
  (count (filled-rows grid)))

(defn clear-filled-rows [grid]
  (let [num-cols (count (first grid))
        num-rows (count grid)
        cleared-grid (remove #(every? true? %) grid)
        new-rows (new-grid num-cols (- num-rows (count cleared-grid)))]
    (vec (concat new-rows cleared-grid))))

(defn new-board [grid [unit & source]]
  {:game-over false
   :deadly-command false
   :source source
   :grid grid
   :unit unit
   :previous-positions [unit]
   :commands []
   :move-scores []})

(defn sum-move-scores
  [[move-score & move-scores] ls-old]
  (if (nil? move-score)
    0
    (let [{:keys [ls size]} move-score
          points (+ size (* 100 (+ 1 ls) (/ ls 2)))
          line-bonus (if (pos? ls-old)
                       (let [floor #?(:clj #(Math/floor %)
                                      :cljs #(js/Math.floor %))]
                         (floor (* (dec ls-old) (/ points 10))))
                       0)]
      (+ points line-bonus (sum-move-scores move-scores ls)))))

(defn score-game [board]
  (if (:deadly-command board)
    0
    (-> (:move-scores board) (sum-move-scores 0))))

(defn board-transition-locking [{:keys [source grid unit] :as board} command]
  (let [old-members (:members unit)
        new-grid (grid-fill grid old-members)
        move-score {:ls (num-filled-rows grid)
                    :size (count old-members)}
        new-board (-> board
                      (update :move-scores conj move-score)
                      (assoc :grid (clear-filled-rows new-grid)))]
    (if-let [next-unit (first source)]
      (cond-> (-> new-board
                  (assoc :unit next-unit
                         :previous-positions [next-unit])
                  (update :source rest))
        (overlaps-filled? (:grid new-board) next-unit) (assoc :game-over true))
      (assoc new-board
             :unit nil
             :previous-positions nil
             :game-over true))))

(defn board-transition-moving [board command]
  (let [neighbor (tetris-neighbor (:unit board) command)]
    (-> board
        (assoc :unit neighbor)
        (update :previous-positions conj neighbor))))

(defn board-transition [board command]
  (let [next-board
        (cond
          (deadly-command? board command)
          (assoc board :game-over true :deadly-command true)
          (locking-command? board command)
          (board-transition-locking board command)
          :otherwise
          (board-transition-moving board command))]
    (update next-board :commands conj command)))
