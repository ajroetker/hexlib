(ns hexlib.loader
  (:require [hexlib.lcg :as lcg]
            [hexlib.tetris :as tetris]))

(defn generate-infinite-source [seed units]
  (let [num-units (count units)]
    (map (comp #(nth units %)
               #(mod % num-units)) (lcg/rand-seq seed))))

(def hex-map->vec (juxt :x :y))
(defn unit-map->vec [unit]
  (-> unit
      (update :pivot hex-map->vec)
      (update :members (partial map hex-map->vec))))

(defn unit-centered [num-cols {:keys [members] :as unit}]
  (let [ys (map second members)
        top-most (apply min ys)
        xs (map first members)
        left-most (apply min xs)
        right-most (apply max xs)
        x-range (inc (- right-most left-most))
        x-center-ideal (int (Math/floor (/ (- num-cols x-range) 2)))
        centering-fn (fn [[col row]]
                       [(+ col x-center-ideal (- left-most))
                        (- row top-most)])]
    (-> unit
        (update :pivot centering-fn)
        (update :members (partial mapv centering-fn)))))

(defn load-boards [{num-cols :width :as problem}]
  (let [{num-rows :height pre-filled :filled
         source-seeds :sourceSeeds source-length :sourceLength
         problem-units :units}
        (-> problem
            (update :units (partial map (comp #(unit-centered num-cols %)
                                              unit-map->vec)))
            (update :filled (partial map hex-map->vec)))
        grid (tetris/new-grid num-cols num-rows pre-filled)]
    (for [source-seed source-seeds
          :let [source (->> problem-units
                            (generate-infinite-source source-seed)
                            (take source-length))]]
      (tetris/new-board grid source))))
