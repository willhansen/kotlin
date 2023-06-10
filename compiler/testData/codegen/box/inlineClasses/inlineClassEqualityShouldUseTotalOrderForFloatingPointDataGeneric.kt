// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineFloat<T: Float>(konst data: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineDouble<T: Double>(konst data: T)

fun box(): String {
    if (InlineFloat(0.0f) == InlineFloat(-0.0f)) throw AssertionError()
    if (InlineFloat(Float.NaN) != InlineFloat(Float.NaN)) throw AssertionError()

    if (InlineDouble(0.0) == InlineDouble(-0.0)) throw AssertionError()
    if (InlineDouble(Double.NaN) != InlineDouble(Double.NaN)) throw AssertionError()

    return "OK"
}
