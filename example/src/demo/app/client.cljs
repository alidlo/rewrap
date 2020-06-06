(ns demo.app.client
  (:require [demo.core :as c :refer [defnc]]
            [demo.router :as rtr]
            [helix.core :refer [$]]
            ["react-native" :as rn]))

(defnc landing []
  [:view {:style {:align-items "center"}}
   [:text "Landing Page"]
   [:text [rtr/link {:to "/app"} "Go to app"]]])

(defnc dashboard []
  [:view {:style {:align-items "center"}}
   [:text "Dashboard Page"]
   [:text [rtr/link {:to "/"} "Go to landing"]]])

(defnc app []
  [rtr/browser-router
   [rtr/routes
    [rtr/route {:path "/app"  :element ($ dashboard)}]
    [rtr/route {:path "/*"    :element ($ landing)}]]])

(defn ^:export mount "Mount application at given `el` id."
  []
  (.registerComponent rn/AppRegistry
                      "Resoflect CMS" #(identity app))
  (.runApplication    rn/AppRegistry
                      "Resoflect CMS" #js {:rootTag (.getElementById js/document "app")}))
