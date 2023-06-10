// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.test.*

class Klass

fun box(): String {
    konst kClass = Klass::class
    konst jClass = kClass.java
    konst kjClass = Klass::class.java
    konst kkClass = jClass.kotlin
    konst jjClass = kkClass.java

    assertEquals("Klass", jClass.getSimpleName())
    assertEquals("Klass", kjClass.getSimpleName())
    assertEquals("Klass", kkClass.java.simpleName)
    assertEquals("Klass", kClass.simpleName)
    assertEquals(kjClass, jjClass)

    try { kClass.members; return "Fail members" } catch (e: Error) {}

    konst jlError = Error::class.java
    konst kljError = Error::class
    konst jljError = kljError.java
    konst jlkError = jlError.kotlin

    assertEquals("Error", jlError.getSimpleName())
    assertEquals("Error", jljError.getSimpleName())
    assertEquals("Error", jlkError.java.simpleName)
    assertEquals("Error", kljError.simpleName)

    return "OK"
}
