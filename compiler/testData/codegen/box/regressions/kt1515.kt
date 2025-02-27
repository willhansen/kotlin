// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: 1.kt

package thispackage

import otherpackage.*

fun box(): String {
    if (!localUse()) {
        return "local use failed"
    }
    if (!fromOtherPackage()) {
        return "use from other package failed"
    }
    return "OK"
}

fun localUse(): Boolean {
    konst c = Runnable::class.java
    return (c.getName()!! == "java.lang.Runnable")
}

// FILE: 2.kt

package otherpackage

fun fromOtherPackage(): Boolean {
    konst c = Runnable::class.java
    return (c.getName()!! == "java.lang.Runnable")
}
