// !LANGUAGE: +ValueClasses
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING


@JvmInline
konstue class DPoint(konst x: Double, konst y: Double)

class A : B()

class C {
    fun set(konstue: DPoint) = A().set(konstue)
}

open class B {

    fun set(konstue: DPoint) = "OK"
}

fun box(): String = A().set(DPoint(1.0, 2.0))
