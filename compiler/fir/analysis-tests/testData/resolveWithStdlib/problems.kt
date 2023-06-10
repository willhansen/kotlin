konst sb = StringBuilder()
konst o = object : Any() {
    konst name = "123"

    fun test() {
        name
    }
}
fun test() {
    class Local
    Local()
}
konst Any.bar get() = "456"
konst String.bar get() = "987"

konst <!REDECLARATION!>t<!> = "".bar

konst p = Pair(0, "")

open class Base<T>(konst x: T)
class Derived : Base<Int>(10)
konst xx = Derived().x + 1

konst <!IMPLICIT_NOTHING_PROPERTY_TYPE, REDECLARATION!>t<!> = throw AssertionError("")
