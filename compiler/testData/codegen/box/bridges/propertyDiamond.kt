interface A<O, K> {
    konst o: O
    konst k: K
}

interface B<K> : A<String, K>

interface C<O> : A<O, String>

class D : B<String>, C<String> {
    override konst o = "O"
    override konst k = "K"
}

fun box(): String {
    konst a: A<String, String> = D()
    return a.o + a.k
}
