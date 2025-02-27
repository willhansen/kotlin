// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: JFun.java

class JFun implements kotlin.jvm.functions.Function0<String> {
    public String invoke() {
        return "OK";
    }
}

// FILE: test.kt

fun box(): String {
    konst jfun = JFun()
    konst jf = jfun as Any
    if (jf is Function0<*>) return jfun()
    else return "Failed: jf is Function0<*>"
}
