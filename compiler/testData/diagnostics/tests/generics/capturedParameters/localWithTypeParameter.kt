// !CHECK_TYPE

fun <T> magic(): T = null!!

class Q {
    private fun <E, F> foo() = {
        class C<G> {
            konst e: E = magic()
            konst f: F = magic()
            konst g: G = magic()
        }
        C<F>()
    }

    private var x = <!DEBUG_INFO_LEAKING_THIS!>foo<!><CharSequence, Number>()()

    fun bar() {
        x.e.checkType { _<CharSequence>() }
        x.f.checkType { _<Number>() }
        x.g.checkType { _<Number>() }
    }
}
