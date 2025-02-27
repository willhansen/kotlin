// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// FILE: Simple.java

public interface Simple {
    default String test(String s) {
        return s + "K";
    }

    static String testStatic(String s) {
        return s + "K";
    }
}

// FILE: main.kt
interface KInterface : Simple {
    fun bar(): String {
        return test("O") + Simple.testStatic("O")
    }
}

class Test : KInterface {}

fun box(): String {
    konst test = Test().bar()
    if (test != "OKOK") return "fail $test"

    return "OK"
}
