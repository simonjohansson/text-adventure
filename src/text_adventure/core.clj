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

(def chain-welded? false)

(def bucket-filled? false)

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

(defn inventory []
  (filter (fn [x] (is-at? x 'body object-location))
          objects))

(defn have? [object]
  (some #{object} (inventory)))

(defmacro defspel [& rest]
  `(defmacro ~@rest))

(defspel walk [direction]
  `(walk-direction '~direction))

(defspel pickup [object]
  `(spel-print (pickup-object '~object)))

(defspel game-action [command subj obj place & args]
  `(defspel ~command [subject# object#]
     `(spel-print (cond (and (= location '~'~place)
                             (= '~subject# '~'~subj)
                             (= '~object# '~'~obj)
                             (have? '~'~subj))
                        ~@'~args
                        :else '(i cannot ~'~command like that -)))))

(game-action weld chain bucket attic
             (cond (and (have? 'bucket) (def chain-welded true))
                   '(the chain is now securely welded to the bucket -)
                   :else '(you do not have a bucket -)))

(game-action dunk bucket well garden
             (cond chain-welded
                   (do (def bucket-filled true)
                       '(the bucket is now full of water))
                   :else '(the water level is too low to reach -)))

(game-action splash bucket wizard living-room
             (cond (not bucket-filled) '(the bucket has nothing in it -)
                   (have? 'frog) '(the wizard awakens and sees that you stole
                                       his frog -
                                       he is so upset he banishes you to the
                                       netherworlds - you lose! the end -)
                   :else '(the wizard awakens from his slumber and greets you
                               warmly -
                               he hands you the magic low-carb donut - you win!
                               the end -)))

(look)
