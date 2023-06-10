// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -USELESS_ELVIS

fun <T> first(x: Array<out T>): T = TODO()
fun <E> elvis(first: E?, second: E): E = TODO()

inline fun <reified M> materializeArray(): Array<M> = TODO()

fun nullableNumbers(): Array<Number?> = TODO()
fun notNullableNumbers(): Array<Number> = TODO()
fun nullableNumbersNullableArray(): Array<Number?>? = TODO()
fun notNullableNumbersNullableArray(): Array<Number>? = TODO()

fun main() {
    konst number1 = first(elvis(notNullableNumbers(), materializeArray()))
    konst number2 = first(elvis(notNullableNumbersNullableArray(), materializeArray()))
    konst number3 = first(notNullableNumbers() ?: materializeArray())
    konst number4 = first(notNullableNumbersNullableArray() ?: materializeArray())
    konst nullableNumber1 = first(elvis(nullableNumbers(), materializeArray()))
    konst nullableNumber2 = first(elvis(nullableNumbersNullableArray(), materializeArray()))
    konst nullableNumber3 = first(nullableNumbers() ?: materializeArray())
    konst nullableNumber4 = first(nullableNumbersNullableArray() ?: materializeArray())

    number1
    number2
    number3
    number4
    nullableNumber1
    nullableNumber2
    nullableNumber3
    nullableNumber4
}
