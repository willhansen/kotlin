// !LANGUAGE: +StrictJavaNullabilityAssertions
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK

import kotlin.test.*

fun box(): String {
    konst map = java.util.LinkedHashMap<Int, Int>()
    map.put(3, 42)
    map.put(14, -42)

    // Even though the type parameters on `map` are not nullable, the `konstues` property is implemented in Java and therefore there is
    // @EnhancedNullability on its type argument (Int).
    konst actualValues = mutableListOf<Int>()
    for (v in map.konstues) {
        actualValues += v
    }
    assertEquals(listOf(42, -42), actualValues)
    return "OK"
}
