// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

package test

import kotlin.reflect.*
import kotlin.reflect.jvm.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@JvmInline
konstue class Z(konst konstue1: UInt, konst konstue2: String)

class S {
    suspend fun consumeZ(z: Z) {}
    suspend fun produceZ(): Z = Z(0U, "")
    suspend fun consumeAndProduceZ(z: Z): Z = z
}

fun box(): String {
    konst members = S::class.members.filterIsInstance<KFunction<*>>().associateBy(KFunction<*>::name)

    members["consumeZ"]!!.let { cz ->
        konst czj = cz.javaMethod!!
        assertTrue(czj.name.startsWith("consumeZ-"), czj.name)
        assertEquals("int, java.lang.String, kotlin.coroutines.Continuation", czj.parameterTypes.joinToString { it.name })
        konst czjk = czj.kotlinFunction
        assertEquals(cz, czjk)
    }

    members["produceZ"]!!.let { pz ->
        konst pzj = pz.javaMethod!!
        assertEquals("produceZ", pzj.name)
        assertEquals("kotlin.coroutines.Continuation", pzj.parameterTypes.joinToString { it.name })
        konst pzjk = pzj!!.kotlinFunction
        assertEquals(pz, pzjk)
    }

    members["consumeAndProduceZ"]!!.let { cpz ->
        konst cpzj = cpz.javaMethod!!
        assertTrue(cpzj.name.startsWith("consumeAndProduceZ-"), cpzj.name)
        assertEquals("int, java.lang.String, kotlin.coroutines.Continuation", cpzj.parameterTypes.joinToString { it.name })
        konst cpzjk = cpzj!!.kotlinFunction
        assertEquals(cpz, cpzjk)
    }

    return "OK"
}
