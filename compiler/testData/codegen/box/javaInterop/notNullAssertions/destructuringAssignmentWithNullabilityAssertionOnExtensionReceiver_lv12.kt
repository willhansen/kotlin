// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: destructuringAssignmentWithNullabilityAssertionOnExtensionReceiver_lv12.kt

import kotlin.test.*

var component1Ekonstuated = false

// NB extension receiver is nullable
operator fun J?.component1() = 1.also { component1Ekonstuated = true }

private operator fun J.component2() = 2

fun use(x: Any) {}

fun box(): String {
    assertFailsWith<NullPointerException> {
        konst (a, b) = J.j()
    }
    if (!component1Ekonstuated) return "component1 should be ekonstuated"
    return "OK"
}


// FILE: J.java
public class J {
    public static J j() { return null; }
}
