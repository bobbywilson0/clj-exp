(ns ^:figwheel-always cvas.core
  (:require [cvas.ui :as ui])
 )

(enable-console-print!)

(ui/draw-screen)
(ui/handle-event ui/mouse-chan)