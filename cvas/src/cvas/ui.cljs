(ns cvas.ui
  (:require [goog.dom :as dom]
            [cljs.core.async :as async :refer [<! >! chan put!]]
            [cvas.state :as state]
            [cvas.unit :as unit])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def board-offset 80)
(def board-width 9)
(def board-height 5)
(def tile-size 64)
(def ctx (.getContext (dom/getElement "canvas") "2d"))
(def mouse-chan (chan))
(set! (.-imageSmoothingEnabled ctx) false)

(defn unit-sprite-path [type]

  (case type
    :red "images/red-default.gif"
    :blue "images/blue-default.gif"
    :ball "images/ball.gif"))

(defn reset-canvas []
  (.setTransform ctx 1, 0, 0, 1, 0.5, 0.5))

(defn draw-tile! [x y]
  (.beginPath ctx)
  (.rect ctx x y tile-size tile-size)
  (set! (.-fillStyle ctx) "white")
  (.fill ctx)
  (set! (.-lineWidth ctx) 0.5)
  (set! (.-strokeStyle ctx) "black")
  (.stroke ctx))

(defn draw-tiles! [xOffset yOffset w h]
  (reset-canvas)
  (.translate ctx xOffset yOffset)
  (doall
    (map
      (fn [y]
        (doall
          (map
            (fn [x] (draw-tile! (* tile-size x) (* tile-size y)))
            (range 0 w))))
      (range 0 h))))

(defn pixels-to-grid [x y]
  [(Math/floor (/ (- x board-offset) tile-size))
   (Math/floor (/ y tile-size))])

(defn grid-to-pixels [x y]
  [(+ (+ (* x tile-size) board-offset) (/ tile-size 4))
   (* y tile-size)])

(defn draw-unit! [unit]
  (let [player-sprite (new js/Image)]
    (set! (.-src player-sprite) (unit-sprite-path (:type unit)))
    ;(set! (.-onload player-sprite)
    player-sprite))

(defn redraw-unit! [unit]
  (reset-canvas)
  (let [player-sprite (new js/Image)
        [x y] (grid-to-pixels (:x unit) (:y unit))]
    (set! (.-src player-sprite) (unit-sprite-path (:type unit)))
    (set! (.-width player-sprite) "32px")
    (set! (.-width player-sprite) "64px")
    (.drawImage ctx player-sprite x y)
    player-sprite))

(defn draw-highlighted-tile! [color x y]
  (.beginPath ctx)
  (.rect ctx x y tile-size tile-size)
  (set! (.-fillStyle ctx) color)
  (.fill ctx)
  (set! (.-lineWidth ctx) 1)
  (set! (.-strokeStyle ctx) "black")
  (.stroke ctx))

(defn mouse-move [e]
  (put! mouse-chan {:type :mouse-move :coords (pixels-to-grid (.-layerX e) (.-layerY e))}))

(defn mouse-down [e]
  (put! mouse-chan {:type :mouse-down :coords (pixels-to-grid (.-layerX e) (.-layerY e))}))

(defn highlight-tile [color x y]
  (let [[pixel-x pixel-y] (grid-to-pixels x y)
        unit (unit/find-one-unit-by x y (:turn @state/game) @state/game)]
    (if (and (< x board-width) (>= x 0) (< y board-height) (>= y 0))
      (if unit
        (do
          (draw-highlighted-tile! color (- pixel-x (/ tile-size 4)) pixel-y)
          (redraw-unit! unit))
        (draw-highlighted-tile! color (- pixel-x (/ tile-size 4)) pixel-y)))))

(.addEventListener (dom/getElement "canvas") "mousedown" mouse-down false)
(.addEventListener (dom/getElement "canvas") "mousemove" mouse-move false)

(defn draw-screen []
  (.clearRect ctx 0 0 (.-width (dom/getElement "canvas")) (.-height (dom/getElement "canvas")))
  (draw-tiles! 0 50 1 3)
  (draw-tiles! board-offset 0 board-width board-height)
  (draw-tiles! 670 50 1 3)

  (if (not (:images-loaded @state/game))
    (do
      (doall (map draw-unit! (:units @state/game)))
      (swap! state/game assoc :images-loaded true))
    (doall (map redraw-unit! (:units @state/game))))
  (apply highlight-tile "yellow" (:selected-tile @state/game)))

(defn select-unit [event]
  (swap! state/game assoc :selected-tile (:coords event))
  (draw-screen))

(defn handle-event [ch]
  (go
    (loop []
      (let [event (<! ch)]
        (case (:type event)
          :mouse-move
          (do
            (draw-screen)
            (apply highlight-tile "#eee" (:coords event)))
          :mouse-down (select-unit event))
        (recur)))))