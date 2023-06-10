sealed class C<out T, out U>
class A<out T>(konst x: T) : C<T, Nothing>()
class B<out U>(konst x: U) : C<Nothing, U>()

fun bar(x: String): C<Int, String> = B(x)
fun baz(x: Any) = "fail: $x"
fun baz(x: String) = x

typealias Z<U> = B<U>

fun box(): String =
    when (konst x = bar("O")) {
        is A -> "fail??"
        is B -> baz(x.x)
    } + when (konst y = bar("K")) {
        is A -> "fail??"
        is Z -> baz(y.x)
        else -> "..."
    }
