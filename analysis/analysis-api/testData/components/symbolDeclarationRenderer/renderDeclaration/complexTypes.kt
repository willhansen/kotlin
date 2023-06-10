package a.b

class C<T, out S> {
    inner class D<R, in P> {

    }
}

interface Test {
    konst x: a.b.C<out CharSequence, *>.D<in List<*>, *>
}