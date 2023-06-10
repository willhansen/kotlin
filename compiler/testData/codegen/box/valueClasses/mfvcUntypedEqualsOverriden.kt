// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import kotlin.math.abs

@JvmInline
konstue class MFVC(konst x: Int, konst y: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is MFVC) {
            return false
        }
        return abs(x - other.x) < 2 && abs(y - other.y) < 2
    }

    override fun hashCode() = 0
}

fun box(): String {
    konst set = setOf(MFVC(1, 2), MFVC(2, 3), MFVC(5, 6))
    return if (set.size == 2) "OK" else "Fail"
}
