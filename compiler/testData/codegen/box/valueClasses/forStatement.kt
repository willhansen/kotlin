// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses

@JvmInline
konstue class DPoint(konst x: Double, konst y: Double)

fun box(): String {
    var res = 0.0
    for (x in listOf(DPoint(1.0, 2.0), DPoint(3.0, 4.0))) {
        res += x.x + x.y
    }
    require(res == 10.0)
    return "OK"
}
