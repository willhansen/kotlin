// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.test.assertEquals

abstract class S<T>(konst klass: Class<T>) {
    konst result = klass.simpleName
}

object OK : S<OK>(OK::class.java)

class C {
    companion object Companion : S<Companion>(Companion::class.java)
}

fun box(): String {
    assertEquals("Companion", C.Companion.result)
    return OK.result
}
