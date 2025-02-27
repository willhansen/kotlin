// WITH_STDLIB
// FULL_JDK
// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND: JVM
// IGNORE_BACKEND: ANDROID
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun blackhole(vararg a: Any?) {}

konst spilledVariables = mutableSetOf<Pair<String, String>>()

var c: Continuation<Unit>? = null

suspend fun saveSpilledVariables() = suspendCoroutineUninterceptedOrReturn<Unit> { continuation ->
    spilledVariables.clear()
    for (field in continuation.javaClass.declaredFields) {
        if (field.name != "label" && (field.name.length != 3 || field.name[1] != '$')) continue
        field.isAccessible = true
        konst fieldValue = when (konst obj = field.get(continuation)) {
            is Array<*> -> obj.joinToString(prefix = "[", postfix = "]")
            else -> obj
        }
        spilledVariables += field.name to "$fieldValue"
    }
    c = continuation
    COROUTINE_SUSPENDED
}

suspend fun test(check: Int) {
    when (check) {
        0 -> {
            konst a = "a0"
            saveSpilledVariables()
            blackhole(a)
        }
        1 -> {
            konst a = "a1"
            konst b = "b1"
            saveSpilledVariables()
            blackhole(a, b)
        }
        else -> {
            konst a = "a2"
            konst b = "b2"
            konst c = 1
            saveSpilledVariables()
            blackhole(a, b, c)
        }
    }
    saveSpilledVariables()
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

fun box(): String {
    builder {
        test(0)
    }

    var continuationName = "Continuation at WhenKt\$box\$1.invokeSuspend(when.kt:62)"
    if (spilledVariables != setOf("label" to "1", "I$0" to "0", "I$1" to "0", "I$2" to "0", "L$0" to continuationName, "L$1" to "a0", "L$2" to "null"))
        return "FAIL 1: $spilledVariables"
    c?.resume(Unit)
    if (spilledVariables != setOf("label" to "4", "I$0" to "0", "I$1" to "0", "I$2" to "0", "L$0" to continuationName, "L$1" to "a0", "L$2" to "[a0]"))
        return "FAIL 2: $spilledVariables"
    c?.resume(Unit)
    if (spilledVariables != setOf("label" to "4", "I$0" to "0", "I$1" to "0", "I$2" to "0", "L$0" to continuationName, "L$1" to "a0", "L$2" to "[a0]"))
        return "FAIL 3: $spilledVariables"

    builder {
        test(1)
    }

    continuationName = "Continuation at WhenKt\$box\$2.invokeSuspend(when.kt:76)"
    if (spilledVariables != setOf("label" to "2", "I$0" to "1", "I$1" to "1", "I$2" to "0", "L$0" to continuationName, "L$1" to "a1", "L$2" to "b1"))
        return "FAIL 4: $spilledVariables"
    c?.resume(Unit)
    if (spilledVariables != setOf("label" to "4", "I$0" to "1", "I$1" to "1", "I$2" to "0", "L$0" to continuationName, "L$1" to "a1", "L$2" to "b1"))
        return "FAIL 5: $spilledVariables"
    c?.resume(Unit)
    if (spilledVariables != setOf("label" to "4", "I$0" to "1", "I$1" to "1", "I$2" to "0", "L$0" to continuationName, "L$1" to "a1", "L$2" to "b1"))
        return "FAIL 6: $spilledVariables"

    builder {
        test(2)
    }

    continuationName = "Continuation at WhenKt\$box\$3.invokeSuspend(when.kt:90)"
    if (spilledVariables != setOf("label" to "3", "I$0" to "2", "I$1" to "2", "I$2" to "1", "L$0" to continuationName, "L$1" to "a2", "L$2" to "b2"))
        return "FAIL 7: $spilledVariables"
    c?.resume(Unit)
    if (spilledVariables != setOf("label" to "4", "I$0" to "2", "I$1" to "2", "I$2" to "1", "L$0" to continuationName, "L$1" to "a2", "L$2" to "b2"))
        return "FAIL 8: $spilledVariables"
    c?.resume(Unit)
    if (spilledVariables != setOf("label" to "4", "I$0" to "2", "I$1" to "2", "I$2" to "1", "L$0" to continuationName, "L$1" to "a2", "L$2" to "b2"))
        return "FAIL 9: $spilledVariables"

    return "OK"
}