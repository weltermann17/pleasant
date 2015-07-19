(in-ns 'pleasant.protocolmonad)

(derive ::applicative ::functor)
(derive ::monad ::applicative)

(derive ::arrow ::category)
(derive ::arrow-first ::arrow)
(derive ::arrow-choice ::arrow-first)
(derive ::arrow-apply ::arrow-choice)

(derive ::arrow ::applicative)
(derive ::arrow-apply ::monad)

(def ^:private arrow-apply (cons [::arrow-apply #'pleasant.protocolmonad/ArrowApply] []))
(def ^:private arrow-choice (cons [::arrow-choice #'pleasant.protocolmonad/ArrowChoice] arrow-apply))
(def ^:private arrow-first (cons [::arrow-first #'pleasant.protocolmonad/ArrowFirst] arrow-apply))
(def ^:private arrow (cons [::arrow #'pleasant.protocolmonad/Arrow] []))
(def ^:private category (cons [::category #'pleasant.protocolmonad/Category] arrow))

(def ^:private monad (cons [::monad #'pleasant.protocolmonad/Monad] arrow-apply))
(def ^:private applicative (cons [::applicative #'pleasant.protocolmonad/Applicative] (concat monad arrow)))
(def ^:private functor (cons [::functor #'pleasant.protocolmonad/Functor] applicative))

(def ^:private hierachy
  (hash-map :arrow-apply arrow-apply
            :arrow-choice arrow-choice
            :arrow-first arrow-first
            :arrow arrow
            :category category
            :monad monad
            :applicative applicative
            :functor functor))

(defn most-general [type inst]
  (some (fn [[t p]] (when (satisfies? (deref p) inst) t))
        (type hierachy)))

;; eof
