(ns credit-engine.credit
  (:require [clojure.core.async :as async :refer [>! >!! <! <!! go]]))

(defmulti handle-credit-msg #(keyword (%2 "type")))

(defmethod handle-credit-msg :reserve-credit
  [client-map msg]
  (println "reserve-credit" msg)
  (let [cur-amt (get-in client-map [(msg "userid") :amount])
        reserve-amt (msg "amount")]       
    (assoc-in client-map [(msg "userid") :amount] (- cur-amt reserve-amt))))

; TODO: maintain reserved-amt, validate rollbacks
(defmethod handle-credit-msg :rollback-credit
  [client-map msg]
  (println "rollback-credit" msg)
  (let [cur-amt (get-in client-map [(msg "userid") :amount])
        rollback-amt (msg "amount")]
    (assoc-in client-map [(msg "userid") :amount] (+ cur-amt rollback-amt))))

(defmethod handle-credit-msg :get-credit
  [client-map msg]
  (println "get-credit" msg)
  client-map)

(defmethod handle-credit-msg :set-credit
  [client-map msg]
  (println "set-credit" msg)  
  (assoc-in client-map [(msg "userid") :amount] (msg "amount")))  

(defmethod handle-credit-msg :default
  [client-map msg]  
  (println "error: credit task received invalid msg" msg ", type" (msg "type"))  
  client-map)

(defn init-task
  []
  (let [in-chan (async/chan 32)
        out-chan (async/chan 32)]
    (go (loop [client-map {}]
      (println "credit task client map" client-map)      
      (let [[client-addr empty-msg msg] (<! in-chan)
            client (String. client-addr)
            updated-map (handle-credit-msg client-map msg)]
          (>! out-chan [client-addr empty-msg (updated-map (msg "userid"))])      
          (recur updated-map))))
    [in-chan out-chan]))