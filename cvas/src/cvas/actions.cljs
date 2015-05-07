(ns cvas.actions
  (:require [cvas.state :as state]
            [cvas.unit :as unit]
            [cvas.ui :as ui]))

(defn switch-turns []
  (if (= (:turn @state/game) :red)
    (swap! state/game assoc :turn :blue :actions 0)
    (swap! state/game assoc :turn :red :actions 0)))

(defn update-unit [unit & to-conj]
  (let [updated-unit   (apply conj unit to-conj)
        filtered-units (filter #(not= unit %) (:units @state/game))]
    (swap! state/game assoc :units (conj filtered-units updated-unit))))

(defn select-unit [{:keys [x y]}]
  (swap! state/game assoc :selected-tile (unit/find-one-unit-by x y (:turn @state/game) @state/game))
  (ui/draw-screen))

(defn deselect-unit [unit]
  (swap! state/game assoc :selected-tile nil))

(defn increment-actions []
  (swap! state/game update :actions inc))


