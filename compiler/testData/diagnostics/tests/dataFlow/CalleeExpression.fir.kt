class C(konst f : () -> Unit)

fun test(e : Any) {
    if (e is C) {
        (e.f)()
    }
}
