(ns how-much.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clj-http.client :as client]
            [hiccup.page :refer [html5]]
            [clj-time.core :as t]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.joda-time]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(def server-token (System/getenv "UBER_SERVER_TOKEN"))

(def home-latitude 51.560895)
(def home-longitude -0.120300)

(def work-latitude 51.530523)
(def work-longitude -0.103285)

(defn get-products []
  (:body (client/get "https://api.uber.com/v1/products"
              {:as :json
               :query-params {:server_token server-token
                              :latitude home-latitude
                              :longitude home-longitude}})))

(defn get-price-estimates []
  (:body (client/get "https://api.uber.com/v1/estimates/price"
              {:as :json
               :query-params {:server_token server-token
                              :start_latitude home-latitude
                              :start_longitude home-longitude
                              :end_latitude work-latitude
                              :end_longitude work-longitude}})))

(defn mongo-connection []
  (let [uri (or (System/getenv "MONGOLAB_URI")
                "mongodb://localhost:27017/how-much")]
    (:db (mg/connect-via-uri uri))))

(defn record-estimates [{:keys [prices]}]
  (let [db (mongo-connection)]
    (mc/insert-batch db "estimates"
                     (map #(assoc % :timestamp (t/now)) prices ))))

(defn render-estimate [{:keys [timestamp low_estimate high_estimate]}]
  [:tr
   [:td timestamp]
   [:td low_estimate]
   [:td high_estimate]])

(defn render-product [[name estimates]]
  [:li
   [:h2 name]
   [:table
    [:thead
     [:tr
      [:th "Time"] [:th "Low"] [:th "High"]]]
    [:tbody
     (map render-estimate estimates)]]])

(defn render-estimates []
  (let [db (mongo-connection)
        estimates (mc/find-maps db "estimates")]
    (html5 [:body
            [:h1 "Hello"]
            [:ul (map render-product (group-by :display_name estimates))]])))

(defroutes app-routes
  (GET "/" []
       (record-estimates (get-price-estimates))
       (render-estimates))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn start [port]
  (jetty/run-jetty app {:port port
                        :join? false}))

(defn -main []
  (let [port (Integer. (or (System/getenv "PORT") "3000"))]
    (start port)))
