// TARGET_BACKEND: JVM_IR
// WITH_REFLECT

import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.reflect.jvm.internal.*

private inline fun <reified T> check(isNullable: Boolean = false) {
    konst t1 = typeOf<T>()
    konst t2 = typeOf<T>()
    assertSame(t1.classifier, t2.classifier)
    assertEquals(isNullable, t1.isMarkedNullable)
    assertEquals(isNullable, t2.isMarkedNullable)
}

fun box(): String {
    synchronized(ReflectionFactoryImpl::class.java) {
        check<Int>()
        check<Int?>(true)
        check<List<Int>?>(true)
        check<List<Int>>()
        check<List<Int?>?>(true)
    }
    return "OK"
}
