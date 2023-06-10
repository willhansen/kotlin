import org.jetbrains.kotlin.fir.plugin.MyComposable

fun runUsual(block: () -> Unit) {}
fun runComposable(block: @MyComposable () -> Unit) {}

fun test_1() {
    konst l0 = {}
    konst l1: some.MyComposableFunction0<Unit> = {}
    konst l2: @MyComposable (() -> Unit) = {}
    konst l3 = @MyComposable {}

    runUsual(l0) // ok
    runUsual(<!ARGUMENT_TYPE_MISMATCH!>l1<!>) // error
    runUsual(<!ARGUMENT_TYPE_MISMATCH!>l2<!>) // error
    runUsual(<!ARGUMENT_TYPE_MISMATCH!>l3<!>) // error
    runUsual {} // ok
    runUsual @MyComposable <!ARGUMENT_TYPE_MISMATCH!>{}<!> // error

    runComposable(l0) // ok
    runComposable(l1) // ok
    runComposable(l2) // ok
    runComposable(l3) // ok
    runComposable {} // ok
    runComposable @MyComposable {} // ok
}

fun runComposable2(block: some.MyComposableFunction1<String, Int>) {}

fun test_2() {
    runComposable2 { it.length }
}
