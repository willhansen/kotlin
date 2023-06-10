// TARGET_BACKEND: JVM
// WITH_REFLECT
package test

import kotlin.test.assertEquals
import kotlin.test.assertTrue

annotation class Anno(@get:JvmName("uglyJvmName") konst konstue: String)

@Anno(konstue = "OK")
class Foo


annotation class Meta(konst anno: Anno)

@Meta(Anno(konstue = "OK"))
fun bar() {}

fun box(): String {
    konst f = Foo::class.annotations.single()
    assertTrue("@test.Anno\\(uglyJvmName=\"?OK\"?\\)".toRegex().matches(f.toString()))
    assertEquals("OK", (f as Anno).konstue)

    konst b = ::bar.annotations.single()
    assertEquals("@test.Meta(anno=$f)", b.toString())
    assertEquals("OK", (b as Meta).anno.konstue)

    return "OK"
}
