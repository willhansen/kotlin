// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses

@JvmInline
konstue class DPoint(konst x: Double, konst y: Double)

fun tryExpr() = try {
    DPoint(0.0, 1.0)
} catch(_: Throwable) {
    DPoint(2.0, 3.0)
} finally {
    DPoint(4.0, 5.0)
}

fun tryBody() {
    try {
        DPoint(0.0, 1.0)
    } catch(_: Throwable) {
        DPoint(2.0, 3.0)
    } finally {
        DPoint(4.0, 5.0)
    }
    konst x: DPoint = try {
        DPoint(0.0, 1.0)
    } catch(_: Throwable) {
        DPoint(2.0, 3.0)
    } finally {
        DPoint(4.0, 5.0)
    }
}


// 1 tryExpr.*(\n  .*)(\n   .*)*(\n   .*box-impl.*)(\n   .*)*(\n   .*box-impl.*)
// 0 tryExpr.*(\n  .*)(\n   .*)*(\n   .*box-impl.*)(\n   .*)*(\n   .*box-impl.*)(\n   .*)*(\n   .*box-impl.*)
// 0 tryBody.*(\n   .*)*(\n   .*box-impl.*)
