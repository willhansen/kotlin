// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.jvm.javaType
import kotlin.test.assertEquals

interface A {
    suspend fun f(param: String): MutableList<in String>
}

fun box(): String {
    konst type = A::class.members.single { it.name == "f" }.returnType.javaType
    assertEquals("java.util.List<? super java.lang.String>", type.toString())

    return "OK"
}
