(ns hexlib.renderer)

(def sqrt #?(:clj #(Math/sqrt %)
             :cljs #(js/Math.sqrt %)))
(def pi #?(:clj Math/PI
           :cljs (.-PI js/Math)))
(def cos #?(:clj #(Math/cos %)
           :cljs #(js/Math.cos %)))
(def sin #?(:clj #(Math/sin %)
            :cljs #(js/Math.sin %)))

(def sqrt-3 (sqrt 3.0))
(def orientation-pointy {:f0 sqrt-3 :f1 (/ sqrt-3 2.0) :f2 0.0 :f3 (/ 3.0 2.0)
                        :b0 (/ sqrt-3 3.0) :b1 (/ (- 1.0) 3.0) :b2 0.0 :b3 (/ 2.0 3.0)
                        :start-angle 0.5})

;; Layout will need to be defined as follows:
;;
;; ~~~clojure
;; (def layout-pointy {:orientation orientation-pointy
;;                    :size [20.0 20.0]
;;                    :origin [27.0 28.0]})
;; ~~~

(defn cube->pixel [layout [x _ z]]
  (let [{{:keys [f0 f1 f2 f3]} :orientation
         [size-x size-y] :size
         [origin-x origin-y] :origin} layout]
    [(+ origin-x (* size-x (+ (* f0 x) (* f1 z))))
     (+ origin-y (* size-y (+ (* f2 x) (* f3 z))))]))

(defn pixel->cube [layout [x y]]
  (let [{{:keys [b0 b1 b2 b3]} :orientation
         [size-x size-y] :size
         [origin-x origin-y] :origin} layout
        pt-x (/ (- x origin-x) size-x)
        pt-y (/ (- y origin-y) size-y)
        cube-x (+ (* b0 pt-x) (* b1 pt-y))
        cube-z (+ (* b2 pt-x) (* b3 pt-y))]
    [cube-x (- (+ cube-x cube-z)) cube-z]))

(defn cube-corner-offset [layout corner]
  (let [{{:keys [start-angle]} :orientation
         [size-x size-y] :size} layout
        angle (* 2.0 pi (/ (+ corner start-angle) 6.0))]
    [(* size-x (cos angle))
     (* size-y (sin angle))]))

(defn polygon-corners [layout cube]
  (let [[center-x center-y] (cube->pixel layout cube)]
    (for [corner (range 6)
          :let [[offset-x offset-y] (cube-corner-offset layout corner)]]
      {:x (+ offset-x center-x)
       :y (+ offset-y center-y)})))
