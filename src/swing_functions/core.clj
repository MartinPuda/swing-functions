(ns swing-functions.core
  (:import (javax.swing JFrame JScrollPane JTextArea BorderFactory JTable)
           (java.awt BorderLayout Dimension)
           (java.awt.event MouseAdapter)
           (java.lang.module ModuleDescriptor$Version)
           (javax.swing.table TableRowSorter))
  (:require [clojure.string :as str])
  (:gen-class))

(defn -main [& args]
  (let [text-panel (doto (JTextArea.)
                     (.setBorder (BorderFactory/createEmptyBorder 10 10 10 10)))

        left-table (doto (JTable. (->> (ns-publics 'clojure.core)
                                       (vals)
                                       (map meta)
                                       (sort-by (juxt #(ModuleDescriptor$Version/parse (or (:added %) "0.0")) :name)
                                                #(compare %2 %1))
                                       (map (juxt :name :added))
                                       (to-array-2d))
                                  (to-array ["Name" "Added"]))
                     (.setAutoCreateRowSorter true)
                     (.addMouseListener (proxy [MouseAdapter] []
                                          (mouseClicked [event]
                                            (let [src (.getSource event)
                                                  row (.getSelectedRow src)]
                                              (let [meta-map (meta (find-var (symbol (str "clojure.core/" (.getValueAt src row 0)))))]
                                                (.setText text-panel
                                                          (->> meta-map
                                                               ((juxt :name :arglists #(str "Added: " (:added %)) :doc))
                                                               (str/join "\n\n")))))))))

        scroll1 (doto (JScrollPane. left-table)
                  (.setPreferredSize (Dimension. 300 400))
                  (.setVerticalScrollBarPolicy JScrollPane/VERTICAL_SCROLLBAR_ALWAYS))

        scroll2 (doto (JScrollPane. text-panel)
                  (.setPreferredSize (Dimension. 550 400)))

        frame (doto (JFrame. "Functions")
                (.setPreferredSize (Dimension. 850 500))
                (.add scroll1 BorderLayout/LINE_START)
                (.add scroll2 BorderLayout/CENTER))
        sorter (TableRowSorter. (.getModel left-table))
        model (.getColumnModel left-table)]

    (.setRowSorter left-table sorter)
    (doto sorter
      (.setModel (.getModel left-table))
      (.setComparator 1 #(.compareTo (ModuleDescriptor$Version/parse %)
                                     (ModuleDescriptor$Version/parse %2))))

    (.setPreferredWidth (.getColumn model 0) 250)
    (.setPreferredWidth (.getColumn model 1) 50)
    (doto frame
      (.pack)
      (.setVisible true))))