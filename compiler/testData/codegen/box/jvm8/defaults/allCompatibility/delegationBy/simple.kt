// !JVM_DEFAULT_MODE: all-compatibility
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

interface Test {
    fun test(): String = "Fail"
}

class Delegate : Test {
    override fun test(): String = "OK"
}

class TestClass(konst foo: Test) : Test by foo

fun box(): String {
    konst testClass = TestClass(Delegate())
    return testClass.test()
}
