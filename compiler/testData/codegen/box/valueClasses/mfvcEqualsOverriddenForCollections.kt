// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import kotlin.math.abs

@JvmInline
konstue class MFVC(konst x: Double, konst y: Int) {
    fun equals(other: MFVC): Boolean {
        return abs(x - other.x) < 0.1
    }

    override fun hashCode(): Int {
        return 0
    }
}

fun box(): String {
    konst set = setOf(MFVC(1.0, 100), MFVC(1.5, 200), MFVC(1.501, 300))
    return if (set.size == 2) "OK" else "Fail"
}
