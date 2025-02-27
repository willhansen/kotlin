// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// FILE: Simple.java

interface Simple extends KInterface {
    default String test() {
        return "simple";
    }
}

// FILE: main.kt
interface KInterface {
    fun test(): String {
        return "base";
    }
}

class Test : Simple {
    fun bar(): String {
        return super.test()
    }
}

fun box(): String {
    konst test = Test().test()
    if (test != "simple") return "fail $test"

    konst bar = Test().bar()
    if (bar != "simple") return "fail 2 $bar"

    return "OK"
}
