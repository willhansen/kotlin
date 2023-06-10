// TARGET_BACKEND: JVM
// WITH_REFLECT
// WITH_COROUTINES

import kotlin.coroutines.startCoroutine
import kotlin.reflect.full.callSuspend
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import helpers.*

inline class Z(konst konstue: Int)

class C {
    private var konstue: Z = Z(0)

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

fun box(): String {
    konst c = C()

    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        run0 {
            C::nonNullConsume.callSuspend(c, Z(1))
            C::nonNullProduce.callSuspend(c).konstue
        }.let { assertEquals(1, it) }
    }

    run0 {
        C::nullableConsume.callSuspend(c, Z(2))
        C::nullableProduce.callSuspend(c)!!.konstue
    }.let { assertEquals(2, it) }

    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        run0 {
            C::nonNull_nonNullConsumeAndProduce.callSuspend(c, Z(3)).konstue
        }.let { assertEquals(3, it) }
    }

    run0 {
        C::nonNull_nullableConsumeAndProduce.callSuspend(c, Z(4))!!.konstue
    }.let { assertEquals(4, it) }

    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        run0 {
            C::nullable_nonNullConsumeAndProduce.callSuspend(c, Z(5)).konstue
        }.let { assertEquals(5, it) }
    }

    run0 {
        C::nullable_nullableConsumeAndProduce.callSuspend(c, Z(6))!!.konstue
    }.let { assertEquals(6, it) }

    return "OK"
}
