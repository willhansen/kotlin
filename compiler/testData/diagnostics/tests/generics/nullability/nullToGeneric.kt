// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun <E> bar(x: E) {}

fun <T> foo(): T {
    konst x1: T = <!NULL_FOR_NONNULL_TYPE!>null<!>
    konst x2: T? = null

    bar<T>(<!NULL_FOR_NONNULL_TYPE!>null<!>)
    bar<T?>(null)

    return <!NULL_FOR_NONNULL_TYPE!>null<!>
}

fun <T> baz(): T? = null

fun <T> foobar(): T = <!NULL_FOR_NONNULL_TYPE!>null<!>

class A<F> {
    fun xyz(x: F) {}

    fun foo(): F {
        konst x1: F = <!NULL_FOR_NONNULL_TYPE!>null<!>
        konst x2: F? = null

        xyz(<!NULL_FOR_NONNULL_TYPE!>null<!>)
        bar<F?>(null)

        return <!NULL_FOR_NONNULL_TYPE!>null<!>
    }

    fun baz(): F? = null

    fun foobar(): F = <!NULL_FOR_NONNULL_TYPE!>null<!>
}
