// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

@JvmInline
konstue class DPoint(konst x: Double, konst y: Double)

class PointBox(var konstue: DPoint)

fun box(): String {
    var p = DPoint(1.0, 2.0)
    try {
        p = DPoint(3.0, error("Failure"))
    } catch (_: Exception) {
    }
    if (p != DPoint(1.0, 2.0)) {
        return "Partially reassigned variable"
    }
    
    konst box = PointBox(p)

    try {
        box.konstue = DPoint(3.0, error("Failure"))
    } catch (_: Exception) {
    }
    
    if (box.konstue != DPoint(1.0, 2.0)) {
        return "Partially reassigned field"
    }
    
    return "OK"
}