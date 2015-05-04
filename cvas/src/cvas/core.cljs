(ns ^:figwheel-always cvas.core
  (:require [goog.dom :as dom]))

(enable-console-print!)


(defonce game
         (atom
           {:units
                        [{:id 1 :type :blue :x 0 :y 0}
                         {:id 3 :type :blue :x 0 :y 2}
                         {:id 5 :type :blue :x 0 :y 4}
                         {:id 6 :type :red :x 8, :y 0}
                         {:id 8 :type :red :x 8, :y 2}
                         {:id 10 :type :red :x 8, :y 4}
                         {:id 11 :type :ball :x 4 :y 0 :dead true}
                         {:id 13 :type :ball :x 4 :y 2 :dead true}
                         {:id 15 :type :ball :x 4 :y 4 :dead true}]
            :blue-bench []
            :red-bench  []
            :turn       :red
            :actions    0
            :hover-tile []
            :images-loaded false}))

(defn unit-sprite-path [type]

  (case type
        :red "images/red-default.gif"
        :blue "images/blue-default.gif"
        :ball "images/ball.gif"))

(def board-offset 80)
(def tile-size 64)
(def ctx (.getContext (dom/getElement "canvas") "2d"))
(set! (.-imageSmoothingEnabled ctx) false)

(defn reset-canvas []
  (.setTransform ctx 1, 0, 0, 1, 0.5, 0.5))

(defn draw-tile! [x y]
  (.beginPath ctx)
  (.rect ctx x y tile-size tile-size)
  (set! (.-fillStyle ctx) "white")
  (.fill ctx)
  (set! (.-lineWidth ctx) 1)
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
  (reset-canvas)
  (let [player-sprite (new js/Image)
        [x y] (grid-to-pixels (:x unit) (:y unit))]
    (set! (.-src player-sprite) (unit-sprite-path (:type unit)))
    (set! (.-width player-sprite) "32px")
    (set! (.-width player-sprite) "64px")
    (set! (.-onload player-sprite) #(.drawImage ctx player-sprite x y))
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

(defn mouse-down-listener [e]
  (println "GRID: " (pixels-to-grid (.-layerX e) (.-layerY e))))

(defn find-one-unit-by [x y col]
  (first
    (filter
      (fn [unit]
        (and
          (= x (:x unit))
          (= y (:y unit))))
      (:units col))))

(defn draw-highlighted-tile! [x y]
  (.beginPath ctx)
  (.rect ctx x y tile-size tile-size)
  (set! (.-fillStyle ctx) "lightgray")
  (.fill ctx)
  (set! (.-lineWidth ctx) 1)
  (set! (.-strokeStyle ctx) "black")
  (.stroke ctx))

(defn select-tile [e]
  (swap! game assoc :hover-tile (pixels-to-grid (.-layerX e) (.-layerY e))))

(defn highlight-tile [x y]
  (let [[pixel-x pixel-y] (grid-to-pixels x y)
        unit (find-one-unit-by x y @game)]
    (if (and (> pixel-x 80) (< pixel-x 650))
      (if unit
        (do
          (draw-highlighted-tile! (- pixel-x (/ tile-size 4)) pixel-y)
          (redraw-unit! unit))
        (draw-highlighted-tile! (- pixel-x (/ tile-size 4)) pixel-y)))))



(.addEventListener (dom/getElement "canvas") "mousedown" mouse-down-listener false)
(.addEventListener (dom/getElement "canvas") "mousemove" select-tile false)


(defn draw-screen []
  (js/requestAnimationFrame #(draw-screen))
  (.clearRect ctx 0 0 (.-width (dom/getElement "canvas")) (.-height (dom/getElement "canvas")))
  (draw-tiles! 0 50 1 3)
  (draw-tiles! board-offset 0 9 5)
  (draw-tiles! 670 50 1 3)

  (if (not (:images-loaded @game))
    (do
      (doall (map draw-unit! (:units @game)))
      (swap! game assoc :images-loaded true))
    (doall (map redraw-unit! (:units @game))))
  (apply highlight-tile (:hover-tile @game)))


(draw-screen)
