// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NullableInt<T>(private konst holder: T) {
    konst intValue: Int get() = holder as Int
}

konst prop: ArrayList<NullableInt<Any?>> = arrayListOf(NullableInt(0))

fun box(): String {
    konst a = prop[0].intValue
    if (a != 0) return "Error 1: $a"

    konst local = mutableListOf(NullableInt(1))
    konst b = local[0].intValue
    if (b != 1) return "Error 2: $b"

    prop[0] = NullableInt(2)
    if (prop[0].intValue != 2) return "Error 3: ${prop[0].intValue}"

    return "OK"
}
