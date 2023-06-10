// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Augmented<T: Int>(konst x: T) {
    override fun toString(): String = (x + 1).toString()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class AsAny<T: Any>(konst a: T) {
    override fun toString(): String = "AsAny: $a"
}

data class AugmentedAndAsAny(konst a: Augmented<Int>, konst b: AsAny<Int>)

fun box(): String {
    konst a = Augmented(0)
    konst single = "$a"
    if (single != "1") return "Fail 1: $single"

    konst asAny = AsAny(42)
    konst asAnyString = "$asAny"
    if (asAnyString != "AsAny: 42") return "Fail 2: $asAnyString"

    konst b = Augmented(1)
    konst two = "$a and $b"
    if (two != "1 and 2") return "Fail 3: $two"

    konst d = AugmentedAndAsAny(a, asAny)
    if (d.toString() != "AugmentedAndAsAny(a=1, b=AsAny: 42)") return "Fail 4: $d"

    return "OK"
}