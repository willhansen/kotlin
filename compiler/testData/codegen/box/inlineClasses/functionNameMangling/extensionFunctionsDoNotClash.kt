// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Id(konst id: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Name(konst name: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Password(konst password: String)

fun Id.test() {
    if (id != "OK") throw AssertionError()
}

fun Id?.test() {
    if (this != null) throw AssertionError()
}

fun Name.test() {
    if (name != "OK") throw AssertionError()
}

fun test(password: Password) {
    if (password.password != "OK") throw AssertionError()
}

class Outer {
    fun Id.testExn() {
        if (id != "OK") throw AssertionError()
    }

    fun Name.testExn() {
        if (name != "OK") throw AssertionError()
    }

    fun testExn(password: Password) {
        if (password.password != "OK") throw AssertionError()
    }

    fun testExns() {
        Id("OK").testExn()
        Name("OK").testExn()
        testExn(Password("OK"))
    }
}

fun box(): String {
    Id("OK").test()
    null.test()
    Name("OK").test()
    test(Password("OK"))

    Outer().testExns()

    return "OK"
}