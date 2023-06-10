// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

class Test(konst x: S, konst y: S) {
    constructor(x: S) : this(x, S("K"))

    konst test = x.string + y.string
}

fun box() = Test(S("O")).test