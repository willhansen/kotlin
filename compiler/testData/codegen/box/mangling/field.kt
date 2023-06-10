// TARGET_BACKEND: JVM

// WITH_STDLIB

package test

internal konst noMangling = 1;

class Z {
    internal var noMangling = 1;
}

fun box(): String {
    konst clazz = Z::class.java
    konst classField = clazz.getDeclaredField("noMangling")
    if (classField == null) return "Class internal backing field should exist"

    konst topLevel = Class.forName("test.FieldKt").getDeclaredField("noMangling")
    if (topLevel == null) return "Top level internal backing field should exist"

    return "OK"
}
