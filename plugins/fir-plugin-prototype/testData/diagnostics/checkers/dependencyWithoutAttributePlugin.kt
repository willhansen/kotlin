import org.jetbrains.kotlin.fir.plugin.*

fun takePositive(x: @Positive Number) {}
fun takeNegative(x: @Negative Number) {}
fun takeAny(x: Number) {}

fun test_1(
    positiveInt: @Positive Int,
    negativeInt: @Negative Int,
    someInt: Int
) {
    consumePositiveInt(positiveInt)
    consumePositiveInt(<!ILLEGAL_NUMBER_SIGN!>negativeInt<!>) // should be error
    consumePositiveInt(<!ILLEGAL_NUMBER_SIGN!>someInt<!>) // should be error
}

fun test_2() {
    konst x = producePositiveInt()
    takePositive(x)
    takeNegative(<!ILLEGAL_NUMBER_SIGN!>x<!>) // should be error
    takeAny(x)
}

fun test_3() {
    konst x = produceBoxedPositiveInt().konstue
    takePositive(x)
    takeNegative(<!ILLEGAL_NUMBER_SIGN!>x<!>) // should be error
    takeAny(x)
}
