// !JVM_DEFAULT_MODE: all-compatibility
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

interface IBase {
    fun bar() = "OK"
}

open class Base {
    fun foo() = "OK"
}

class C : Base(), IBase {
    konst lambda1 = {
        super.foo()
    }

    konst lambda2 = {
        super.bar()
    }
}

fun box(): String {
    if (C().lambda1() != "OK") return "fail 1"

    return C().lambda2()
}
