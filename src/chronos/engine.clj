(ns chronos.engine
  (:require [clojure.set :refer [difference union map-invert]]))


(def document-workflow
  {:nodes {:source_to_image {} :barcodes {} :ocr {} :lang {}}
   :depends-on {:barcodes #{:source_to_image}
                :ocr #{:source_to_image}
                :lang #{:ocr}}})


(defn get-entrypoints
  "Return all nodes which don't have any entrypoints and are therefore suitable entrypoints."
  [graph]
  (let [nodes (set (keys (:nodes graph)))
        nodes-with-dependencies (set (keys (:depends-on graph)))]
    (difference nodes nodes-with-dependencies)))


(defn invert-edges
  [edges]
  (reduce-kv
   (fn [acc par deps]
     (reduce #(update %1 %2 (fnil conj #{}) par) acc deps))
   {}
   edges))


(defn complete-graph
  [graph]
  (assoc graph :required-by (invert-edges (get graph :depends-on))))


(defn traverse
  [graph]
  (let [graph-spec (complete-graph graph)
        entrypoints (get-entrypoints graph-spec)]
    (loop [completed #{}
           pending entrypoints]
      (when (not-empty pending)
        (let [node (first pending)
              next-nodes (determine-next-nodes
                          node
                          (conj completed node)
                          (:required-by graph-spec)
                          (:depends-on graph-spec))]
          (println node)
          (recur (conj completed node) (union (disj pending node) next-nodes)))))))


(defn ready?
  "Check whether a nodes dependencies have been satisfied."
  [node dependencies completed]
  (every? #(contains? completed %) (get dependencies node)))


(defn determine-next-nodes
  "Find suitable next nodes after current-node has completed."
  [current-node completed required-by dependent-on]
  (let [dependent-nodes (get required-by current-node)]
    (into #{} (filter #(ready? % dependent-on completed) dependent-nodes))))


(next-nodes :c #{:b :a :c} {:b #{:c} :c #{:d :e}} {:c #{:b} :d #{:c} :e #{:c}})
(traverse document-workflow)
