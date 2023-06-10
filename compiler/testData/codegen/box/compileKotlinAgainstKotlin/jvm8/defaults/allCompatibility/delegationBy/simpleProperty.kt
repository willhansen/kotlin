// !JVM_DEFAULT_MODE: all-compatibility
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

// MODULE: lib
// FILE: 1.kt
interface Test {
    konst test: String get() = "Fail"
}

class Delegate : Test {
    override konst test: String get() = "OK"
}

// MODULE: main(lib)
// FILE: 2.kt
class TestClass(konst foo: Test) : Test by foo

fun box(): String {
    konst testClass = TestClass(Delegate())
    return testClass.test
}
