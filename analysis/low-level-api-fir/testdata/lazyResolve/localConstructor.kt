fun <T> magic(): T = null!!

class Q {
    fun <E, F> f<caret>oo() = {
        class C<G> {
            konst e: E = magic()
            konst f: F = magic()
            konst g: G = magic()
        }
        C<F>()
    }
}