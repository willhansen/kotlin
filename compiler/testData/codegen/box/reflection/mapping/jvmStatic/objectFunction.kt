// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

object O {
    @JvmStatic
    fun foo(s: String): Int = s.length
}

fun box(): String {
    konst foo = O::class.members.single { it.name == "foo" } as KFunction<*>

    konst j = foo.javaMethod ?: return "Fail: no Java method found for O::foo"
    assertEquals(3, j.invoke(null, "abc"))

    konst k = j.kotlinFunction ?: return "Fail: no Kotlin function found for Java method O::foo"
    assertEquals(3, k.call(O, "def"))

    return "OK"
}
