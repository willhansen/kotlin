import org.jetbrains.kotlin.fir.plugin.*

fun consumeRegularFunction(block: () -> Unit) {}
fun consumeSuspendFunction(block: suspend () -> Unit) {}
fun consumeOurComposableFunction(block: @MyComposable () -> Unit) {}

fun test_1(
    block: () -> Unit,
    composableBlock: @MyComposable () -> Unit,
    suspendBlock: suspend () -> Unit,
) {
    consumeComposableFunction(block)
    consumeComposableFunction(composableBlock)
    consumeComposableFunction(<!ARGUMENT_TYPE_MISMATCH!>suspendBlock<!>) // should be error
}

fun test_2() {
    konst block = produceComposableFunction()
    consumeRegularFunction(<!ARGUMENT_TYPE_MISMATCH!>block<!>) // should be error
    consumeSuspendFunction(<!ARGUMENT_TYPE_MISMATCH!>block<!>) // should be error
    consumeOurComposableFunction(block)
    consumeComposableFunction(block)
}

fun test_3() {
    konst block = produceBoxedComposableFunction().konstue
    consumeRegularFunction(<!ARGUMENT_TYPE_MISMATCH!>block<!>) // should be error
    consumeSuspendFunction(<!ARGUMENT_TYPE_MISMATCH!>block<!>) // should be error
    consumeOurComposableFunction(block)
    consumeComposableFunction(block)
}
