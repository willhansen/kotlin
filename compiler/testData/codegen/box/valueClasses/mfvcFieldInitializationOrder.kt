// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

@JvmInline
konstue class DPoint(konst x: Double, konst y: Double)

class A(var x: DPoint) {
    init { x = DPoint(2.0, 3.0) }
}

class B() {
    var a = DPoint(4.0, 5.0)
    konst b = a
    init { a = DPoint(6.0, 7.0) }
    konst c = a
}

fun box(): String {
    require(A(DPoint(0.0, 1.0)).x == DPoint(2.0, 3.0)) { "No init used" }
    require(B().a == DPoint(6.0, 7.0)) { "No init used" }
    require(B().b == DPoint(4.0, 5.0)) { "Wrong init order used" }
    require(B().c == DPoint(6.0, 7.0)) { "Wrong init order used" }
    return "OK"
}