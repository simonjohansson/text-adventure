(ns text-adventure.core)

(def objects '(whiskey-bottle bucket frog chain))

(def game-map (hash-map
               'living-room '((you are in the living room
                                   of a wizards house - there is a wizard
                                   snoring loudly on the couch -)
                              (west door garden)
                              (upstairs stairway attic))
               'garden '((you are in a beautiful garden -
                              there is a well in front of you -)
                         (east door living-room))
               'attic '((you are in the attic of the
                             wizards house - there is a giant
                             welding torch in the corner -)
                        (downstairs stairway living-room))))

(def object-location (hash-map
                      'whiskey-bottle 'living-room
                      'bucket 'living-room
                      'chain 'garden
                      'frog 'garden))

(def location 'living-room)

(defn describe-location [location game-map]
  (first (location game-map)))

(defn describe-path [path]
  `(There is a ~(second path) going ~(first path) from here -))

(defn describe-paths [location game-map]
  (apply concat (map describe-path (rest (get game-map location)))))

(defn spel-print [list]
  (map (fn [x] (symbol (name x)))
       list))

(defn is-at? [obj loc obj-loc]
  (= (obj obj-loc) loc))

(defn describe-floor [loc objs obj-loc]
  (apply concat (map (fn [x]
                       `(you see a ~x on the floor -))
                     (filter (fn [x] (is-at? x loc obj-loc))
                             objs))))

(defn look []
  (spel-print (concat (describe-location location game-map)
                      (describe-paths location game-map)
                      (describe-floor location objects object-location))))

(defn walk-direction [direction]
  (let [next (first (filter (fn [x] (= direction (first x)))
                            (rest (location game-map))))]
    (cond next (do (def location (nth next 2))
                   (look))
          :else '(Hey! Where do you think you are going? -))))

(defn pickup-object [object]
  (cond (is-at? object location object-location)
        (do (def object-location (assoc object-location object 'body))
            `(You are now carrying the ~object))
        :else '(nah, you cant get that.)))

(defmacro defspel [& rest]
  `(defmacro ~@rest))

(defspel walk [direction]
  `(walk-direction '~direction))

(defspel pickup [object]
  `(spel-print (pickup-object '~object)))
