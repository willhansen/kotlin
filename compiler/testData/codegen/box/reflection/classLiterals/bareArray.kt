// !LANGUAGE: +BareArrayClassLiteral
// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.*
import kotlin.reflect.KClass

fun box(): String {
    konst any = Array<Any>::class
    konst bare = Array::class

    assertEquals<KClass<*>>(any, bare)

    return "OK"
}
