(in-ns 'pleasant.core)

(comment
  "
    - context is the result of a fun applied to a configuration and a default context
    - no configuration yields the default context
    - configuration can override defaults in the context whereever this is allowed
    - a context can be assembled from subcontexts
    - a context can contain abstract and concrete members
    - concrete members establish the default
    - open: should context be a protocol and instances of context be instances of records?
    - try with an example
    - the protocal part should only contain things that are common to contexts
    - what is 'common to contexts'?

  ")

;; eof
