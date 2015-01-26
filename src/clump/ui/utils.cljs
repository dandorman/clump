(ns clump.ui.utils
  (:require [clojure.string :refer [join]]))

(defn defer [f]
  (.setTimeout js/window f 0))

(defn alert [msg]
  (defer #(js/alert msg)))

(defprotocol IClassNames
  (class-names [this]))

(extend-type cljs.core.PersistentVector
  IClassNames
  (class-names [v] (apply str (interpose " " (set (map name v))))))

(extend-type cljs.core.PersistentArrayMap
  IClassNames
  (class-names [m] (->> m
                        (filter (fn [[k v]] v))
                        (keys)
                        (join " "))))
