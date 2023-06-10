// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// WITH_COROUTINES
// LANGUAGE: +ValueClasses

import kotlin.coroutines.startCoroutine
import kotlin.reflect.full.callSuspend
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import helpers.*

@JvmInline
konstue class Z(konst konstue1: UInt, konst konstue2: Int)

class C {
    private var konstue: Z = Z(0U, 0)

    suspend fun nonNullConsume(z: Z) { konstue = z }
    suspend fun nonNullProduce(): Z = konstue
    suspend fun nullableConsume(z: Z?) { konstue = z!! }
    suspend fun nullableProduce(): Z? = konstue
    suspend fun nonNull_nonNullConsumeAndProduce(z: Z): Z = z
    suspend fun nonNull_nullableConsumeAndProduce(z: Z): Z? = z
    suspend fun nullable_nonNullConsumeAndProduce(z: Z?): Z = z!!
    suspend fun nullable_nullableConsumeAndProduce(z: Z?): Z? = z
}

private fun run0(f: suspend () -> Int): Int {
    var result = -1
    f.startCoroutine(handleResultContinuation { result = it })
    return result
}

private fun run0U(f: suspend () -> UInt): UInt {
    var result = UInt.MAX_VALUE
    f.startCoroutine(handleResultContinuation { result = it })
    return result
}

fun box(): String {
    konst c = C()

    run0U {
        C::nonNullConsume.callSuspend(c, Z(1U, -1))
        C::nonNullProduce.callSuspend(c).konstue1
    }.let { assertEquals(1U, it) }

    run0 {
        C::nonNullConsume.callSuspend(c, Z(1U, -1))
        C::nonNullProduce.callSuspend(c).konstue2
    }.let { assertEquals(-1, it) }

    run0U {
        C::nullableConsume.callSuspend(c, Z(2U, -2))
        C::nullableProduce.callSuspend(c)!!.konstue1
    }.let { assertEquals(2U, it) }
    run0 {
        C::nullableConsume.callSuspend(c, Z(2U, -2))
        C::nullableProduce.callSuspend(c)!!.konstue2
    }.let { assertEquals(-2, it) }

    run0U {
        C::nonNull_nonNullConsumeAndProduce.callSuspend(c, Z(3U, -3)).konstue1
    }.let { assertEquals(3U, it) }
    run0 {
        C::nonNull_nonNullConsumeAndProduce.callSuspend(c, Z(3U, -3)).konstue2
    }.let { assertEquals(-3, it) }

    run0U {
        C::nonNull_nullableConsumeAndProduce.callSuspend(c, Z(4U, -4))!!.konstue1
    }.let { assertEquals(4U, it) }
    run0 {
        C::nonNull_nullableConsumeAndProduce.callSuspend(c, Z(4U, -4))!!.konstue2
    }.let { assertEquals(-4, it) }

    run0U {
        C::nullable_nonNullConsumeAndProduce.callSuspend(c, Z(5U, -5)).konstue1
    }.let { assertEquals(5U, it) }
    run0 {
        C::nullable_nonNullConsumeAndProduce.callSuspend(c, Z(5U, -5)).konstue2
    }.let { assertEquals(-5, it) }

    run0U {
        C::nullable_nullableConsumeAndProduce.callSuspend(c, Z(6U, -6))!!.konstue1
    }.let { assertEquals(6U, it) }
    run0 {
        C::nullable_nullableConsumeAndProduce.callSuspend(c, Z(6U, -6))!!.konstue2
    }.let { assertEquals(-6, it) }

    return "OK"
}
