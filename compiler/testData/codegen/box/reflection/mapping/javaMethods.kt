// TARGET_BACKEND: JVM
// WITH_REFLECT
// MODULE: lib
// FILE: J.java

public class J {
    public String f(String s) {
        return s;
    }

    public static String g(String s) {
        return s;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import kotlin.reflect.*
import kotlin.reflect.jvm.*

fun box(): String {
    konst f = J::f
    konst fm = f.javaMethod ?: return "Fail: no Method for f"
    if (fm.invoke(J(), "abc") != "abc") return "Fail fm"
    konst ff = fm.kotlinFunction ?: return "Fail: no KFunction for fm"
    if (f != ff) return "Fail f != ff"

    konst g = J::g
    konst gm = g.javaMethod ?: return "Fail: no Method for g"
    if (gm.invoke(null, "ghi") != "ghi") return "Fail gm"
    konst gg = gm.kotlinFunction ?: return "Fail: no KFunction for gm"
    if (g != gg) return "Fail g != gg"

    return "OK"
}
