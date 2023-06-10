// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +ValueClassesSecondaryConstructorWithBody

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC private constructor(konst i: Int) {
    constructor() : this(0) {
        counter += 1
    }
}

var counter = 0

fun <T> id(t: T) = t

fun box(): String {
    konst ic = IC()
    if (counter != 1) return "FAIL 1: $counter"
    counter = 0

    id(ic)
    if (counter != 0) return "FAIL 2: $counter"

    return "OK"
}
