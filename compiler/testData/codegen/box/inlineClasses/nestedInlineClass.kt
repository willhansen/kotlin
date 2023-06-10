// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

class C {

    OPTIONAL_JVM_INLINE_ANNOTATION
    konstue class IC1(konst s: String)

    companion object {

        OPTIONAL_JVM_INLINE_ANNOTATION
        konstue class IC2(konst s: String)
    }
}

object O {

    OPTIONAL_JVM_INLINE_ANNOTATION
    konstue class IC3(konst s: String)
}

interface I {

    OPTIONAL_JVM_INLINE_ANNOTATION
    konstue class IC4(konst s: String)
}

fun box(): String {
    if (C.IC1("OK").s != "OK") return "FAIL 1"
    if (C.Companion.IC2("OK").s != "OK") return "FAIL 2"
    if (O.IC3("OK").s != "OK") return "FAIL 3"
    if (I.IC4("OK").s != "OK") return "FAIL 4"
    return "OK"
}