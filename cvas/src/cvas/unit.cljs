(ns cvas.unit
  (:require [cvas.state :as state]))

(defn find-one-unit-by [x y unit-type col]
  (first
    (filter
      (fn [unit]
        (and
          (= x (:x unit))
          (= y (:y unit))
          (= unit-type (:type unit))))
      (:units col))))

(defn unit-adjacent-to-ball? [{ball-x :x ball-y :y}  {unit-x :x unit-y :y}]
  (or
    (and
      (= unit-x (dec ball-x))
      (= unit-y ball-y))
    (and
      (= unit-x (inc ball-x))
      (= unit-y ball-y))
    (and
      (= unit-y (dec ball-y))
      (= unit-x ball-x))
    (and
      (= unit-y (inc ball-y))
      (= unit-x ball-x))))

(defn pickup-ball [selected-unit unit]
  (let [updated-unit   (conj selected-unit {:ball unit :selected false})
        filtered-units (filter
                         (apply every-pred [#(not= unit %) #(not= selected-unit %)])
                         (:units @state/game))]
    (if (unit-adjacent-to-ball? unit selected-unit)
      (swap! state/game assoc :units (conj filtered-units updated-unit)))))

(defn selected? [x y]
  (let [unit (state/selected-unit)]
    (and
      (= x (:x unit))
      (= y (:y unit)))))

(defn in-movement-range? [x2 y2 selected-unit]
  (let [{:keys [:x :y]} selected-unit]
    (and
      (or
        (and
          (= :blue (:turn @state/game))
          (<= x2 4))
        (and
          (= :red (:turn @state/game))
          (>= x2 4)))
      (<=
        (reduce
          +
          (map distance [x2 y2] [x y]))
        move-range))))