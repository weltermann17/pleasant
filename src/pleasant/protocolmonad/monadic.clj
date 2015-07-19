(in-ns 'pleasant.protocolmonad)

(require
  '[clojure.core.match :refer [match]])

;; Functor
(defmulti fmap+
          (fn [f v] (most-general :functor v)))

(defn fmap [f v]
  (if (satisfies? Functor v) (-fmap v f)
                             (fmap+ f v)))

;; Pure
(defmulti pure+
          (fn [a v] (most-general :applicative a)))

(defn pure [m a]
  (if (satisfies? Pure m) (-pure m a)
                          (pure+ m a)))

;; Applicative
(declare <*)

(defmulti <*>+
          (fn [af & _] (most-general :applicative af)))

(defn <*>
  ([af] (fmap #(%) af))
  ([af av & avs] (cond
                   avs (apply <*> (<* af av) avs)
                   (satisfies? Applicative af) (-ap af av)
                   :else (<*>+ af av))))

(defn <*
  "partial application in an applicative"
  ([af] af)
  ([af a & r] (if r (apply <* (<* af a) r)
                    (<*> (fmap (fn [f] #(partial f %)) af) a))))

(defn sequence-a [[a & as]]
  (apply <*> (pure a vector) a as))

;; Functor
(defmethod fmap+ ::applicative [f v]
  (<*> (pure v f) v))

;; Category
(defmulti id+
          (fn [c] (most-general :category c)))

(defn id [c]
  (if (satisfies? Category c) (-id c)
                              (id+ c)))

(defmulti comp+
          (fn [a b] (most-general :category a)))

(defn compose [a b]
  (if (satisfies? Category a) (-comp a b)
                              (comp+ a b)))

(defn <<<
  ([a] a)
  ([a b & c] (if c (compose a (apply <<< b c))
                   (compose a b))))

(defn >>>
  ([a] a)
  ([a b & c] (if c (compose (apply >>> b c) a)
                   (compose b a))))

;; Monad
(defmulti >>=+
          (fn [m f] (most-general :monad m)))

(defn >>=
  ([m] m)
  ([m f & fs] (cond
                fs (apply >>= (>>= m f) fs)
                (satisfies? Monad m) (-bind m f)
                :else (>>=+ m f))))

;; Applicative
(defmethod <*>+ ::monad [af av]
  (>>= af (fn [f] (>>= av (fn [v] (pure af (f v)))))))

;; more monad operations
(defn m-do*
  ([body] (m-do* body false))
  ([body type]
   (match [body]
          [([val] :seq)]
          (match val
                 [:return v] `(pure ~type ~v)
                 v v)
          [([fst & rst] :seq)]
          (match fst
                 [:let & vs] `(let [~@vs] ~(m-do* rst type))
                 [:return v] `(>>= (pure ~type ~v) (fn [_#] ~(m-do* rst type)))
                 [:guard v] `(>>= (if ~v (pure ~type nil) (zero ~type))
                                  (fn [_#] ~(m-do* rst type)))
                 [k v] (if type
                         `(>>= ~v (fn [~k] ~(m-do* rst type)))
                         (let [t `t#]
                           `(let [~t ~v]
                              (>>= ~t (fn [~k] ~(m-do* rst t))))))
                 [k v & rs] (m-do* (concat [[k v]] [rs] rst) type)
                 v (if type
                     `(>>= ~v (fn [_#] ~(m-do* rst type)))
                     (let [t `t#]
                       `(let [~t ~v]
                          (>>= ~t (fn [_#] ~(m-do* rst t))))))))))

(defmacro m-do [& body]
  (m-do* body))

(defn lift [f]
  (fn [& m-args] (m-do [args (sequence-a m-args)]
                       [:return (apply f args)])))

(defn join [m]
  (>>= m identity))

(defn >=>
  ([f] f)
  ([f & fs] (fn [& args] (apply >>= (apply f args) fs))))

;; Monoid
(defn zero [m]
  (-zero m))

(defn plus [a & as]
  (cond
    (satisfies? MonoidSum a) (-sum a as)
    (satisfies? Monoid a) (reduce -plus a as)
    :else (throw (UnsupportedOperationException. "Monoid/-plus"))))

;; Arrow
(declare &&&)

(defn -swap [[a b]] [b a])
(defn -dup [a] [a a])

(defn $
  "function application operator"
  [f & args]
  (apply f args))

;; Arrow
(defmulti arr+
          (fn [a f] (most-general :arrow a)))

(defn arr [a f]
  (if (satisfies? Arrow a)
    (-arr a f)
    (arr+ a f)))

(defmulti arr-first+
          (fn [a] (most-general :arrow-first a)))

(defn arr-first [arr]
  (if (satisfies? ArrowFirst arr)
    (-first arr)
    (arr-first+ arr)))

;; Applicative
(defmethod pure+ ::arrow [m a]
  (arr m (fn [_] a)))

(defmethod <*>+ ::arrow [af av]
  (>>> (&&& af av)
       (arr af (fn [[f v]] (f v)))))

;; other arrow functions
(defn arr-second [a]
  (if (satisfies? ArrowSecond a)
    (-second a)
    (>>> (arr a -swap) (arr-first a) (arr a -swap))))

;; clojure complains about ***
(defn ^:dynamic *** [f g]
  (>>> (arr-first f) (arr-second g)))

(defn &&& [f g]
  (>>> (arr f -dup) (*** f g)))

;; ArrowChoice
(defmulti left+
          (fn [a] (most-general :arrow-choice a)))

(defn left [arr]
  (if (satisfies? ArrowChoice arr)
    (-left-a arr)
    (left+ arr)))

(defn right [a]
  (if (satisfies? ArrowChoiceRight arr)
    (-right arr)
    (>>> (arr a mirror)
         (left a)
         (arr a mirror))))

(defn +++ [f g]
  (>>> (left f) (right g)))

(defn ||| [f g]
  (>>> (+++ f g) (arr f #(match [%]
                                [{:left v}] v
                                [{:right v}] v))))

;; ArrowApply
(defmulti app+
          (fn [a] (most-general :arrow-apply a)))

(defn app [arr]
  (if (satisfies? ArrowApply arr)
    (-app arr)
    (app+ arr)))

;; ArrowChoice
(defmethod left+ ::arrow-apply [a]
  (>>> (arr a (||| #(vector (>>> a (arr a left)) %)
                   #(vector (arr a right) %)))
       (app a)))

;; ArrowFirst
(defmethod arr-first+ ::arrow-apply [a]
  (>>> (arr a (fn [[x y]] (>>> (arr a (fn [_] [a x]))
                               (app a)
                               (arr a (fn [v] [v y])))))
       (arr a (fn [v] [v nil]))
       (app a)))

;; Monad
(defmethod >>=+ ::arrow-apply [m f]
  (>>> (&&& (>>> m (arr m f))
            (id m))
       (app m)))

;; Applicative
(prefer-method pure+ ::arrow ::monad)
(prefer-method <*>+ ::arrow ::monad)

;; ArrowLoop
(defn arr-loop [m]
  (-loop m))

;; ArrowProc
(defn -symbols [res exp]
  (filter (into #{} (-> res :vals keys)) (flatten [exp])))

(defn -proc-line [res line]
  (match line
         [:return exp] (-proc-line res [(arr (:type res) identity) exp])
         [a exp] (if (:type res)
                   (let [ss (-symbols res exp)
                         arr-exp (arr a (fn [v] (apply (eval `(fn [~@ss] ~exp)) v)))]
                     (-> res
                         (update-in [:arr] >>>
                                    (arr a (fn [v] [(map #(v (% (:vals res))) (-symbols res exp)) v]))
                                    (arr-first arr-exp)
                                    (arr-first a)
                                    (arr a (fn [[x v]] (assoc v (:index res) x))))
                         (update-in [:index] inc)))
                   (-proc-line (assoc res
                                 :type a
                                 :arr (arr a (:args res))) line))
         [pat a exp] (-> (-proc-line res [a exp])
                         (update-in [:vals] assoc pat (:index res)))))

(defn proc* [args body]
  (reduce -proc-line {:vals  (zipmap args (range))
                      :arr   nil
                      :args  (fn [& as]
                               (assert (= (count as) (count args)))
                               (into [] as))
                      :index (count args)
                      :type  nil} body))



(defn -quote-body [body]
  (mapv (fn [line] (match line
                          [:return exp] [:return `(quote ~exp)]
                          [a exp] [a `(quote ~exp)]
                          [pat a exp] [`(quote ~pat) a `(quote ~exp)]))
        body))

(defmacro proc [args & body]
  `(proc* '~args ~(-quote-body body)))

;; eof
