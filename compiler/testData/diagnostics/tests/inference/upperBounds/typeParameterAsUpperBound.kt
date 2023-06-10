// !CHECK_TYPE


@<!INVISIBLE_MEMBER!>kotlin.internal.<!INVISIBLE_REFERENCE!>InlineOnly<!><!>
public inline fun <C, R> C.ifEmpty(f: () -> R): R where C : Collection<*>, C : R = if (isEmpty()) f() else this

public fun <T> listOf(t: T): List<T> = TODO()


fun usage(c: List<String>) {
    konst cn = c.ifEmpty { null }
    cn checkType { _<List<String>?>() }

    konst cs = c.ifEmpty { listOf("x") }
    cs checkType { _<List<String>>() }
}
