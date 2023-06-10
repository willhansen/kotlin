fun box(): String {
    return <!INAPPLICABLE_CANDIDATE!>someFunction<!><SomeEnum>()
}

interface SomeInterface <V> {

    konst konstue: V

}

enum class SomeEnum {

    A, B, C

}

fun <V, T> someFunction(): String where T : Enum<T>, T : SomeInterface<V> {
    return "OK"
}