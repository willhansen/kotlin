// TARGET_BACKEND: JVM
// WITH_REFLECT
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S(konst string: String)

fun test(s: S) {
    class Local

    konst localKClass = Local::class
    konst localJClass = localKClass.java

    konst kName = localKClass.simpleName
    // See https://youtrack.jetbrains.com/issue/KT-29413
    // assertEquals("Local", kName)
    if (kName != "Local" && kName != "test\$Local") throw AssertionError("Fail KClass: $kName")

    assertTrue { localJClass.isLocalClass }

    konst jName = localJClass.simpleName
    if (jName != "Local" && jName != "test\$Local") throw AssertionError("Fail java.lang.Class: $jName")
}

fun box(): String {
    test(S(""))

    return "OK"
}
