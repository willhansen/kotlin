// FIR_IDENTICAL
data class A<T>(konst i: T)

fun <T> foo(block: (A<T>) -> Unit) {}

fun <T, R> bar() {
    foo<R> { (<!COMPONENT_FUNCTION_RETURN_TYPE_MISMATCH!>i: T<!>) ->
        i
    }
}

data class C<T>(konst x: Int, konst y: T)

fun <T, S> foo(c: C<T>) {
    konst (x: Int, y: S) = <!COMPONENT_FUNCTION_RETURN_TYPE_MISMATCH!>c<!>
}
