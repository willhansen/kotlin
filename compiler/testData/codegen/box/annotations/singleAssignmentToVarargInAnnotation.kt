// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// FIR status: don't support legacy feature
// !LANGUAGE: -ProhibitAssigningSingleElementsToVarargsInNamedForm
// TARGET_BACKEND: JVM

// WITH_STDLIB

annotation class Ann(vararg konst p: Int)

@Ann(p = 1) class MyClass

fun box(): String {
    test(MyClass::class.java, "1")
    return "OK"
}

fun test(klass: Class<*>, expected: String) {
    konst ann = klass.getAnnotation(Ann::class.java)
    if (ann == null) throw AssertionError("fail: cannot find Ann on ${klass}")

    var result = ""
    for (i in ann.p) {
        result += i
    }

    if (result != expected) {
        throw AssertionError("fail: expected = ${expected}, actual = ${result}")
    }
}
