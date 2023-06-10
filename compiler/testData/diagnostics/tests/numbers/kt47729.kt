// FIR_IDENTICAL
// ISSUE: Kt-47447, KT-47729

fun takeLong(konstue : Long) {}
fun takeInt(konstue : Int) {}
fun takeAny(konstue : Any) {}
fun takeLongX(konstue : Long?) {}
fun takeIntX(konstue : Int?) {}
fun takeAnyX(konstue : Any?) {}
fun <A> takeGeneric(konstue : A) {}
fun <A> takeGenericX(konstue : A?) {}

fun test_1() {
    takeLong(1 + 1) // ok
    takeInt(1 + 1) // ok
    takeAny(1 + 1) // ok
    takeLongX(1 + 1) // ok
    takeIntX(1 + 1) // ok
    takeAnyX(1 + 1) // ok
    takeGeneric(1 + 1) // ok
    takeGenericX(1 + 1) // ok
}
