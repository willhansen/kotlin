konst ((T) -> G).foo<P> { }
konst ((T) -> G).foo get{ }
konst ((T) -> G).foo<P>
konst ((T) -> G).foo: = 0
konst ((T) -> G)?.foo
konst ((T) -> G)??.foo

konst (T<T>.(A<B>, C<D, E>) -> ).foo {}
konst konst @a T<T>.(A<B>).foo()

konst @[a] (T<T>.(A<B>)).foo()
konst @[a] ((A<B>)-).foo()

konst c<T> by A.B