(ns hexlib.core)

(defn cube->odd-r [[x y z]]
  (let [col (+ x (/ (- z (bit-and z 1)) 2))
        row z]
    [col row]))

(defn odd-r->cube [[col row]]
  (let [x (- col (/ (- row (bit-and row 1)) 2))
        z row]
    [x (- (+ x z)) z]))

(def directions [[1 (- 1) 0] [1 0 (- 1)] [0 1 (- 1)]
                 [(- 1) 1 0] [(- 1) 0 1] [0 (- 1) 1]])

(defn cube-add [[x1 y1 z1] [x2 y2 z2]]
  [(+ x1 x2) (+ y1 y2) (+ z1 z2)])

(defn cube-negate [hex]
  (mapv - hex))

(defn cube-direction [direction]
  (nth directions direction))

(defn cube-neighbor [hex direction]
  (cube-add hex (cube-direction direction)))

(defn cube-rotation* [[x y z] clockwise?]
  (if clockwise?
    [(- z) (- x) (- y)]
    [(- y) (- z) (- x)]))

(defn cube-rotation [hex pivot clockwise?]
  (-> hex
      (cube-add (cube-negate pivot))
      (cube-rotation* clockwise?)
      (cube-add pivot)))

(defn odd-r-neighbor [hex direction]
  (-> hex
      odd-r->cube
      (cube-neighbor direction)
      cube->odd-r))

(defn odd-r-rotation [hex pivot clockwise?]
  (let [cube-hex (odd-r->cube hex)
        cube-pivot (odd-r->cube pivot)]
    (-> cube-hex
        (cube-rotation cube-pivot clockwise?)
        cube->odd-r)))
