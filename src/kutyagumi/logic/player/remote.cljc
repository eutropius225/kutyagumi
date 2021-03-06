(ns kutyagumi.logic.player.remote
  (:require [kutyagumi.logic.player.core #?@(:cljs [:refer [Player]])]
            [clojure.core.async :as async]
            [kutyagumi.misc.network :as nw]
            [clojure.edn :as edn])
  #?(:clj (:import (kutyagumi.logic.player.core Player))))

(defrecord RemotePlayer [in out]
  Player
  (next-move [_ _game]
    (async/go
      (async/>! out {:type :move})
      (edn/read-string (async/<! in))))
  (update-state [this state]
    (async/go
      (async/>! out {:type  :sync
                     :state state})
      (while (async/poll! in))
      this)))

(defn ->remote-player [id state uri]
  (async/go
    (let [[in out] (nw/make-connection uri :host id)]
      (async/>! out {:type  :sync
                     :state state})
      (->RemotePlayer in out))))
