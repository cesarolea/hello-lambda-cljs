(ns hello-lambda-cljs.core
  (:require [aws-sdk :as aws]
            [cljs.pprint :refer [pprint]]
            (cljs.core.async :as async
                             :refer [<! >! chan close! put! timeout]))
  (:require-macros [cljs.core.async.macros :as m :refer [go go-loop]]))

;; set AWS credentials from profile
;; when running in AWS don't set AWS_PROFILE and the SDK
;; will take credentials from the IAM role assigned to the lambda
(when (-> js/process .-env .-AWS_PROFILE)
  (set! (.-credentials aws/config)
        (aws/SharedIniFileCredentials.
         #js {:profile (-> js/process .-env .-AWS_PROFILE)})))
(def s3 (aws/S3.))

(defn <<< [f & args]
  (let [c (chan)]
    (apply f (concat args [#(put! c [%1 %2])]))
    c))

(defn handler
  "Lambda main entry point"
  [_ _ callback]
  (go
    (let [[error buckets] (<! (<<< #(.listBuckets s3 %)))]
      (callback nil
                (clj->js {:status 200
                          :body {:s3-buckets buckets
                                 :error error}
                          :headers {}})))))

;; set your lambda handler as hello_lambda_cljs.handler
(set! (.-exports js/module) #js {:handler handler})

(comment
  ;; how to use the <<< function
  (go (pprint (<! (<<< #(.listBuckets s3 %)))))

  ;; using core.async to coordinate three processes
  (def c (chan))

  (defn simulated-request [c request-type]
    (go
      (let [seconds (* (rand 5) 1000)
            _ (<! (timeout seconds))]
        (>! c {:response request-type :time seconds}))))

  (defn process-actions [c]
    (go-loop [responses []]
      (let [{:keys [response time] :as r} (<! c)]
        (println "I'm done:" response ". Took" time "ms.")
        (if (= (count responses) 2)
          (do
            (println "All done! This was the result:")
            (pprint (conj responses r)))
          (recur (conj responses r))))))

  (simulated-request c :dynamodb-request)
  (simulated-request c :kinesis-push)
  (simulated-request c :s3-download)

  (process-actions c))
