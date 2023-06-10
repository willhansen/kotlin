// !LANGUAGE: +JvmFieldInInterface
// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

interface Foo {
    companion object {
        @JvmField
        konst konstue = "OK"
    }
}

fun box(): String {
    konst field = Foo.Companion::konstue.javaField!!
    return field.get(null) as String
}
