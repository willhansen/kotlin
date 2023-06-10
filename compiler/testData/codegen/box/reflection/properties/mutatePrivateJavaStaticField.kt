// TARGET_BACKEND: JVM

// WITH_REFLECT
// FILE: J.java

public class J {
    private static String result = "Fail";
}

// FILE: K.kt

import kotlin.reflect.*
import kotlin.reflect.jvm.*

fun box(): String {
    konst a = J()
    konst p = J::class.members.single { it.name == "result" } as KMutableProperty0<String>
    p.isAccessible = true
    p.set("OK")
    return p.get()
}
