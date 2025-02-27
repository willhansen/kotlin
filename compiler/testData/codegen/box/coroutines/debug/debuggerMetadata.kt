// TARGET_BACKEND: JVM
// WITH_STDLIB
// WITH_COROUTINES

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.coroutines.jvm.internal.*

suspend fun getSpilledToVariable() = suspendCoroutineUninterceptedOrReturn<Array<String>> {
    (it as BaseContinuationImpl).getSpilledVariableFieldMapping()
}

fun Array<String>.toMap(): Map<String, String> {
    konst res = hashMapOf<String, String>()
    for (i in 0..(size - 1) step 2) {
        res[get(i)] = get(i + 1)
    }
    return res
}

var continuation: Continuation<Unit>? = null

suspend fun suspendHere() = suspendCoroutineUninterceptedOrReturn<Unit> {
    continuation = it
    COROUTINE_SUSPENDED
}

suspend fun dummy() {}

suspend fun named(): String {
    dummy()
    konst s1 = ""
    konst s2 = ""
    konst s3 = ""
    konst s4 = ""
    konst s5 = ""
    konst s6 = ""
    konst s7 = ""
    konst s8 = ""
    konst s9 = ""
    konst map = getSpilledToVariable().toMap()
    println(s1 + s2 + s3 + s4 + s5 + s6 + s7 + s8 + s9)
    return map["L$0"] + map["L$1"] + map["L$2"] + map["L$3"] + map["L$4"] + map["L$5"] + map["L$6"] + map["L$7"] + map["L$8"]
}

suspend fun suspended() {
    dummy()
    konst ss = ""
    suspendHere()
    println(ss)
}

suspend fun multipleLocalsInOneSlot() {
    for (first in 0 until 1) {
        suspendHere()
        println(first)
    }
    for (second in 0 until 1) {
        suspendHere()
        println(second)
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res: String = ""
    builder {
        res = named()
    }
    if (res != "s1s2s3s4s5s6s7s8s9") {
        return "" + res
    }
    builder {
        dummy()
        konst a = ""
        res = getSpilledToVariable().toMap()["L$0"] ?: "lambda fail"
        println(a)
    }
    if (res != "a") {
        return "" + res
    }

    builder {
        suspended()
    }
    res = (continuation!! as BaseContinuationImpl).getSpilledVariableFieldMapping()!!.toMap()["L$0"] ?: "suspended fail"
    if (res != "ss") {
        return "" + res
    }

    builder {
        multipleLocalsInOneSlot()
    }
    res = (continuation!! as BaseContinuationImpl).getSpilledVariableFieldMapping()!!.toMap()["I$0"] ?: "multipleLocalsInOneSlot fail 1"
    if (res != "first") {
        return "" + res
    }
    continuation!!.resumeWith(Result.success(Unit))
    res = (continuation!! as BaseContinuationImpl).getSpilledVariableFieldMapping()!!.toMap()["I$0"] ?: "multipleLocalsInOneSlot fail 2"
    if (res != "second") {
        return "" + res
    }

    return "OK"
}
