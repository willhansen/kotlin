konst (@[a] T<T>.(A<B>) -> Unit).foo: P
konst (@[a] T<T>.(A<B>) ->  C<D, E>).foo: P
konst @[a] (@[a] T<T>.(A<B>) -> R).foo: P
konst <A, B> @[a] (() -> Unit).foo: P
@[a] konst <A, B> @[a] ((A, B) -> Unit).foo: P
