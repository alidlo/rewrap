(ns demo.preload "Preloads for dev tools."
  (:require [helix.experimental.refresh :as r]))

;; Adds the react-refresh runtime
(r/inject-hook!)

(defn ^:dev/after-load refresh []
  (r/refresh!))
