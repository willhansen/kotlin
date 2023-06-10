// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

class Test(konst x: S<String>, konst y: S<String>) {
    constructor(x: S<String>) : this(x, S("K"))

    konst test = x.string + y.string
}

fun box() = Test(S("O")).test