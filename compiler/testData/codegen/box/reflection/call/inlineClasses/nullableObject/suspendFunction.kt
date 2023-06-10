// TARGET_BACKEND: JVM
// WITH_REFLECT
// WITH_COROUTINES

import kotlin.coroutines.startCoroutine
import kotlin.reflect.full.callSuspend
import kotlin.test.assertEquals
import helpers.*

inline class S(konst konstue: String?)

class C {
    private var konstue: S = S("")

    suspend fun nonNullConsume(z: S) { konstue = z }
    suspend fun nonNullProduce(): S = konstue
    suspend fun nullableConsume(z: S?) { konstue = z!! }
    suspend fun nullableProduce(): S? = konstue
    suspend fun nonNull_nonNullConsumeAndProduce(z: S): S = z
    suspend fun nonNull_nullableConsumeAndProduce(z: S): S? = z
    suspend fun nullable_nonNullConsumeAndProduce(z: S?): S = z!!
    suspend fun nullable_nullableConsumeAndProduce(z: S?): S? = z
}

private fun run0(f: suspend () -> String): String {
    var result = ""
    f.startCoroutine(handleResultContinuation { result = it })
    return result
}

fun box(): String {
    konst c = C()

    run0 {
        C::nonNullConsume.callSuspend(c, S("nonNull"))
        C::nonNullProduce.callSuspend(c).konstue!!
    }.let { assertEquals("nonNull", it) }

    run0 {
        C::nullableConsume.callSuspend(c, S("nullable"))
        C::nullableProduce.callSuspend(c)!!.konstue!!
    }.let { assertEquals("nullable", it) }

    run0 {
        C::nonNull_nonNullConsumeAndProduce.callSuspend(c, S("nonNull_nonNull")).konstue!!
    }.let { assertEquals("nonNull_nonNull", it) }

    run0 {
        C::nonNull_nullableConsumeAndProduce.callSuspend(c, S("nonNull_nullable"))!!.konstue!!
    }.let { assertEquals("nonNull_nullable", it) }

    run0 {
        C::nullable_nonNullConsumeAndProduce.callSuspend(c, S("nullable_nonNull")).konstue!!
    }.let { assertEquals("nullable_nonNull", it) }

    run0 {
        C::nullable_nullableConsumeAndProduce.callSuspend(c, S("nullable_nullable"))!!.konstue!!
    }.let { assertEquals("nullable_nullable", it) }

    return "OK"
}
