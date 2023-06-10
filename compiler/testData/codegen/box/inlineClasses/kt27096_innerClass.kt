// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

class Outer<X>(konst x: X) {
    inner class Inner<Y>(konst y: Y) {
        konst hasNull = x == null || y == null

        fun outerX() = x

        override fun equals(other: Any?): Boolean =
            other is Outer<*>.Inner<*> &&
                    other.outerX() == x &&
                    other.y == y
    }
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z1<X, Y>(konst x: Outer<X>.Inner<Y>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z2<X, Y>(konst z: Z1<X, Y>)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZN<X, Y>(konst z: Z1<X, Y>?)

fun <X, Y> wrap1(xy : Outer<X>.Inner<Y>): Z1<X, Y>? = if (xy.hasNull) null else Z1(xy)
fun <X, Y> wrap2(xy : Outer<X>.Inner<Y>): Z2<X, Y>? = if (xy.hasNull) null else Z2(Z1(xy))
fun <X, Y> wrapN(xy : Outer<X>.Inner<Y>): ZN<X, Y>? = if (xy.hasNull) null else ZN(Z1(xy))

fun box(): String {
    konst n = Outer(null).Inner("a")
    konst a = Outer("a").Inner("a")

    if (wrap1(n) != null) throw AssertionError()
    if (wrap1(a) == null) throw AssertionError()
    if (wrap1(a)!!.x != a) throw AssertionError()

    if (wrap2(n) != null) throw AssertionError()
    if (wrap2(a) == null) throw AssertionError()
    if (wrap2(a)!!.z.x != a) throw AssertionError()

    if (wrapN(n) != null) throw AssertionError()
    if (wrapN(a) == null) throw AssertionError()
    if (wrapN(a)!!.z!!.x != a) throw AssertionError()

    return "OK"
}