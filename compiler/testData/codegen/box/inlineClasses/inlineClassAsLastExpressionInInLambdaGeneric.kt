// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Name<T: String>(private konst konstue: T) {
    fun asValue(): String = konstue
}

fun <T: String> concat(a: Name<T>, b: Name<T>) = a.asValue() + b.asValue()

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt<T: Int>(private konst konstue: T) {
    fun asValue(): Int = konstue
}

fun box(): String {
    konst o = inlinedRun {
        Name("O")
    }

    konst k = notInlinedRun {
        Name("K")
    }

    if (concat(o, k) != "OK") return "fail 1"

    konst a = UInt(1)
    konst one = inlinedRun {
        a
    }

    if (one.asValue() != 1) return "fail 2"

    konst b = UInt(2)
    konst two = notInlinedRun {
        b
    }

    if (two.asValue() != 2) return "fail 3"

    return "OK"
}

inline fun <R> inlinedRun(block: () -> R): R = block()
fun <R> notInlinedRun(block: () -> R): R = block()