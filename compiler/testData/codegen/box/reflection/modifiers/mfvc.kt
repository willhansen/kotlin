// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses
// FILE: box.kt

import kotlin.test.assertTrue
import kotlin.test.assertFalse
@JvmInline
konstue class V(konst konstue: String, konst konstue1: String)

fun box(): String {
    assertFalse(V::class.isSealed)
    assertFalse(V::class.isData)
    assertFalse(V::class.isInner)
    assertFalse(V::class.isCompanion)
    assertFalse(V::class.isFun)
    assertTrue(V::class.isValue)

    return "OK"
}
