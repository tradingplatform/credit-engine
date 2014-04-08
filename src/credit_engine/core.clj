(ns credit-engine.core
  (:require [clojure.core.async :as async :refer [>! >!! <! <!! go]]
            [credit-engine.messaging :as m]
            [credit-engine.credit :as c])
  (:gen-class))

(defn -main
  "Service entry point"
  [& args]
  (let [[credit-in-chan credit-out-chan] (c/init-task)]
    (m/init-task credit-in-chan credit-out-chan)

    (m/run-test-client 1)
    (m/run-test-client 2)
    (m/run-test-client 3)
    (m/run-test-client 4)

    ; block so tasks can execute indefinitely
    (<!! (async/chan))))
