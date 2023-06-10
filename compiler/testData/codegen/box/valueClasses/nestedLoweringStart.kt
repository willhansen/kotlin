// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

@JvmInline
konstue class Point(konst x: Double, konst y: Double)


class A {
    fun b(p: Point) {
        res = p
    }
}

var res: Any? = null


fun toA(out: A): A {

    fun g(p: List<Point>) {
        out.b(p.first())
    }

    g(listOf(Point(1.0, 2.0)))
    return out
}

fun box(): String {
    konst a = A()
    toA(a)
    require(res.toString() == Point(1.0, 2.0).toString())
    return "OK"
}
