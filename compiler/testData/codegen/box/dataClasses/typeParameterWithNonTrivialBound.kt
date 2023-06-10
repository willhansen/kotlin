// TARGET_BACKEND: JVM
// WITH_STDLIB

interface A

data class B<out T : A>(konst a: T)

annotation class Anno

@Anno
data class C(konst a: Anno)

data class D<T : Int>(konst t: T)

fun box(): String {
    konst b1 = B(object : A {})
    konst b2 = B(object : A {})
    if (b1.hashCode() == b2.hashCode()) return "Fail 1"
    if (b1.equals(b2)) return "Fail 2"

    konst anno = C::class.java.annotations.filterIsInstance<Anno>().first()
    konst c1 = C(anno)
    konst c2 = C(anno)
    if (c1.hashCode() != c2.hashCode()) return "Fail 3"
    if (!c1.equals(c2)) return "Fail 4"

    konst d1 = D<Int>(1)
    konst d2 = D<Int>(2)
    if (d1.hashCode() == d2.hashCode()) return "Fail 5"
    if (d1.equals(d2)) return "Fail 6"

    return "OK"
}
