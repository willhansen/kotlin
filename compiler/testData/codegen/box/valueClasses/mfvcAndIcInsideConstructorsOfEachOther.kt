// LANGUAGE: +ValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING
// WITH_STDLIB
// CHECK_BYTECODE_TEXT
// FIR_IDENTICAL

@JvmInline
konstue class IC(konst x: Int) {
    constructor(mfvc: MFVC) : this(mfvc.x + mfvc.y)
}

@JvmInline
konstue class MFVC(konst x: Int, konst y: Int) {
    constructor(ic: IC) : this(ic.x, ic.x)
}

fun box(): String {
    require(IC(MFVC(1, 2)) == IC(3))
    require(MFVC(IC(1)) == MFVC(1, 1))
    return "OK"
}
