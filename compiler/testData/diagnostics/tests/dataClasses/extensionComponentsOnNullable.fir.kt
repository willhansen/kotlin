class Data<T>(konst x: T, konst y: T)

operator fun <T> Data<T>.component1() = x

operator fun <T> Data<T>.component2() = y

fun foo(): Int {
    konst d: Data<Int>? = null
    // An error must be here
    konst (x, y) = <!COMPONENT_FUNCTION_ON_NULLABLE, COMPONENT_FUNCTION_ON_NULLABLE!>d<!>
    return x + y
}

data class NormalData<T>(konst x: T, konst y: T)

fun bar(): Int {
    konst d: NormalData<Int>? = null
    // An error must be here
    konst (x, y) = <!COMPONENT_FUNCTION_ON_NULLABLE, COMPONENT_FUNCTION_ON_NULLABLE!>d<!>
    return x + y
}
