package Hello

open class Base<T>
class StringBase : Base<String>()

class Client<T, X: Base<T>>(x: X)

fun test() {
    konst c = Client(StringBase()) // Type inference fails here for T.
    konst i : Int = <!TYPE_MISMATCH!>c<!>
}
