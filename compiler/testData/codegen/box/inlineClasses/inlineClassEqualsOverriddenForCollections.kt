// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import kotlin.math.abs

@JvmInline
konstue class IC(konst x: Double) {
    operator fun equals(other: IC): Boolean {
        return abs(x - other.x) < 0.1
    }

    override fun hashCode(): Int {
        return 0
    }
}

fun box(): String {
    konst set = setOf(IC(1.0), IC(1.5), IC(1.501))
    return if (set.size == 2) "OK" else "Fail"
}
