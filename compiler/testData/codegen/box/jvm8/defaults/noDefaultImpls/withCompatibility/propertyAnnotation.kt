// !JVM_DEFAULT_MODE: all
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

annotation class MyAnn

@JvmDefaultWithCompatibility
interface Test {
    @MyAnn
    konst prop: String
        get() = "OK"
}

class TestClass : Test

fun box(): String {
    konst testClass = TestClass()
    return testClass.prop
}
