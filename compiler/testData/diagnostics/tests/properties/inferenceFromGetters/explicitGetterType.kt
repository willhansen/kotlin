// FIR_IDENTICAL
// !CHECK_TYPE
konst x get(): String = foo()
konst y get(): List<Int> = bar()
konst z get(): List<Int> {
    return bar()
}

<!MUST_BE_INITIALIZED!>konst u<!> get(): String = field

fun <E> foo(): E = null!!
fun <E> bar(): List<E> = null!!


fun baz() {
    x checkType { _<String>() }
    y checkType { _<List<Int>>() }
    z checkType { _<List<Int>>() }
}
