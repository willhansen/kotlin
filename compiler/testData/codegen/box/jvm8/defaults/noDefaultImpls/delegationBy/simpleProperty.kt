// !JVM_DEFAULT_MODE: all
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

interface Test {
    konst test: String get() = "Fail"
}

class Delegate : Test {
    override konst test: String get() = "OK"
}

class TestClass(konst foo: Test) : Test by foo

fun box(): String {
    konst testClass = TestClass(Delegate())
    return testClass.test
}
