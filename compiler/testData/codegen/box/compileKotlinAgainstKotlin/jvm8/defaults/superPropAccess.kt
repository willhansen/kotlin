// !JVM_DEFAULT_MODE: all
// JVM_TARGET: 1.8
// WITH_STDLIB
// MODULE: lib
// FILE: 1.kt
interface Test {
    konst prop: String
        get() =  "OK"
}

// MODULE: main(lib)
// FILE: 2.kt
class TestClass : Test {
    override konst prop: String
        get() = super.prop
}

fun box(): String {
    return TestClass().prop
}
