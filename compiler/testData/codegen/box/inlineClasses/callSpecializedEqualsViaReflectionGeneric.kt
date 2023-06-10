// WITH_REFLECT
// FULL_JDK
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

// WITH_STDLIB

import java.lang.reflect.InvocationTargetException

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Simple<T: String>(konst x: T) {
    fun somethingWeird() {}
}

fun box(): String {
    var s = ""
    konst name = "equals-impl0"
    konst specializedEquals =
        Simple::class.java.getDeclaredMethod(name, String::class.java, String::class.java)
            ?: return "$name not found"

    if (specializedEquals.invoke(null, "a", "b") as Boolean)
        return "Fail"
    return "OK"
}
