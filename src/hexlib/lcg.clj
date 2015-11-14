(ns hexlib.lcg)

(def ^:const multiplier (BigInteger/valueOf 1103515245))
(def ^:const increment (BigInteger/valueOf 12345))
(def ^:const modulus (.shiftLeft BigInteger/ONE 32))
(def ^:const bitmask (BigInteger/valueOf 0x7fff0000))

(defn rand-seq
  [seed]
  (let [next-rand (fn [x] (-> (.multiply x multiplier)
                              (.add increment)
                              (.mod modulus)))
        x->result (fn [x] (.shiftRight (.and x bitmask) 16))]
    (->> (BigInteger/valueOf seed)
         (iterate next-rand)
         (map x->result))))
