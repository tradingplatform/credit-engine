(ns credit-engine.messaging
  (:require [clojure.core.async :as async :refer [>! >!! <! <!! go]]
            [com.keminglabs.zmq-async.core :refer [register-socket!]]
            [clojure.data.json :as json]))

(defn encode
  [m]
  (json/write-str m))

(defn decode
  [msg]
  (json/read-str msg))

(defn init-task
  [out-chan in-chan]
  (let [addr "inproc://credit-engine"
        sock-in (async/chan 32)
        sock-out (async/chan 32)]
    (register-socket! {:in sock-in :out sock-out :socket-type :router
                       :configurator (fn [socket] (.bind socket addr))})
    ; reader
    (go 
      (loop [] 
        (let [[client-addr empty-msg in-msg] (<! sock-out)         
              in-msg (decode (String. in-msg))]          
          (>! out-chan [client-addr empty-msg in-msg])
        (recur))))

    ; writer
    (go 
      (loop [] 
        (let [[client-addr empty-msg out-msg] (<! in-chan)]   
          (>! sock-in [client-addr empty-msg (encode out-msg)]))
        (recur)))))

; TODO: extract to separate test-client namespace
(defn run-test-client
  [userid]
  (let [addr "inproc://credit-engine"
        client-sock-in (async/chan 32)
        client-sock-out (async/chan 32)]
    (register-socket! {:in client-sock-in :out client-sock-out :socket-type :dealer
                       :configurator (fn [socket] (.connect socket addr))})  
    (go    
      (let [msg1 (encode {:type "set-credit" :userid userid :amount 10000})
            msg2 (encode {:type "reserve-credit" :userid userid :amount 7500})
            msg3 (encode {:type "reserve-credit" :userid userid :amount 2500})
            msg4 (encode {:type "rollback-credit" :userid userid :amount 7500})
            msg5 (encode {:type "get-credit" :userid userid})]

          ; initial empty string required for dealer socket per zeromq spec
          (>! client-sock-in ["" msg1])

          ; ignore initial received empty msg 
          (let [in-msg (decode (String. (second (<! client-sock-out))))]
            (println userid "received reply" in-msg)) 

          (>! client-sock-in ["" msg2])
          (let [in-msg (decode (String. (second (<! client-sock-out))))]
            (println userid "received reply" in-msg)) 

          (>! client-sock-in ["" msg3])
          (let [in-msg (decode (String. (second (<! client-sock-out))))]
            (println userid "received reply" in-msg)) 

          (>! client-sock-in ["" msg4])
          (let [in-msg (decode (String. (second (<! client-sock-out))))]
            (println userid "received reply" in-msg)) 

          (>! client-sock-in ["" msg5])
          (let [in-msg (decode (String. (second (<! client-sock-out))))]
            (println userid "received reply" in-msg)) 
                            
      (async/close! client-sock-in)))))
