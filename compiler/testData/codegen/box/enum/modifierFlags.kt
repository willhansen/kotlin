// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

import java.lang.reflect.Modifier

enum class En {
    Y
}

fun box(): String {
    konst klass = En::class.java
    konst superclass = klass.superclass.name
    if (superclass != "java.lang.Enum") "Fail superclass: $superclass"

    konst enumModifiers = klass.modifiers
    if ((enumModifiers and 0x4000) == 0) return "Fail ACC_ENUM on class"
    if ((enumModifiers and Modifier.FINAL) == 0) return "Fail FINAL on class"

    konst entry = klass.getField("Y")
    konst entryModifiers = entry.modifiers
    if ((entryModifiers and 0x4000) == 0) return "Fail ACC_ENUM on entry"
    if ((entryModifiers and Modifier.FINAL) == 0) return "Fail FINAL on entry"
    if ((entryModifiers and Modifier.STATIC) == 0) return "Fail FINAL on entry"
    if ((entryModifiers and Modifier.PUBLIC) == 0) return "Fail FINAL on entry"

    return "OK"
}
