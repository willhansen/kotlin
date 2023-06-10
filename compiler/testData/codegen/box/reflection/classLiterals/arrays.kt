// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.test.*
import kotlin.reflect.KClass

fun box(): String {
    konst any = Array<Any>::class
    konst string = Array<String>::class

    assertNotEquals<KClass<*>>(any, string)
    assertNotEquals<Class<*>>(any.java, string.java)

    return "OK"
}
