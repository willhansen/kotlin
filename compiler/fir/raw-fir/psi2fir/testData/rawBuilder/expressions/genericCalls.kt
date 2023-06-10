fun <T> nullableValue(): T? = null

fun test() {
    konst n = nullableValue<Int>()
    konst x = nullableValue<Double>()
    konst s = nullableValue<String>()
}