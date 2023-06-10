// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineLong(konst konstue: Long)
inline konst Number.toInlineLong get() = InlineLong(this.toLong())

fun box(): String {
    konst konstue = 0

    konst withoutSubject = when (konstue.toInlineLong) {
        0.toInlineLong -> true
        else -> false
    }
    if (!withoutSubject) return "Fail: without subject"

    konst withSubject = when (konst subject = konstue.toInlineLong) {
        0.toInlineLong -> true
        else -> false
    }
    if (!withSubject) return "Fail: with subject"

    return "OK"
}
