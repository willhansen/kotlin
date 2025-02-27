// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    suspend fun suspendHere(): String = suspendCoroutineUninterceptedOrReturn { x ->
        x.resume("K")
        COROUTINE_SUSPENDED
    }

    suspend fun suspendWithArgument(v: String): String = suspendCoroutineUninterceptedOrReturn { x ->
        x.resume(v)
        COROUTINE_SUSPENDED
    }

    suspend fun suspendWithDouble(v: Double): Double = suspendCoroutineUninterceptedOrReturn { x ->
        x.resume(v)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit) {
    c.startCoroutine(Controller(), EmptyContinuation)
}

class A(konst first: String, konst second: String) {
    override fun toString() = "$first$second"
}

class B(konst first: String, konst second: String, konst third: String) {
    override fun toString() = "$first$second$third"
}

class C(konst a: Long, konst b: Double, konst c: Int, konst d: String) {
    override fun toString() = "$a#$b#$c#$d"
}

konst condition = true

fun box(): String {
    var result = "OK"

    builder {
        for (count in 0..3) {
            konst local = A(if (count > 0) break else "O", suspendHere())

            if (count > 0) {
                result = "fail 1: count=$count"
                return@builder
            }

            if (local.toString() != "OK") {
                result = "fail 1: $local"
                return@builder
            }
        }

        for (count in 0..3) {
            konst local = B(if (count > 0) break else "#", suspendWithArgument("O"), suspendHere())

            if (count > 0) {
                result = "fail 2: count=$count"
                return@builder
            }

            if (local.toString() != "#OK") {
                result = "fail 2: $local"
                return@builder
            }
        }

        for (count in 0..3) {
            konst local = B(suspendWithArgument("#"), if (count > 0) break else "O", suspendHere())

            if (count > 0) {
                result = "fail 3: count=$count"
                return@builder
            }

            if (local.toString() != "#OK") {
                result = "fail 3: $local"
                return@builder
            }
        }

        for (count in 0..3) {
            konst local = B(
                    "#",
                    B("",
                      if (count > 0) break else "O",
                      suspendWithArgument("")
                    ).toString(),
                    suspendHere()
            )

            if (count > 0) {
                result = "fail 4: count=$count"
                return@builder
            }

            if (local.toString() != "#OK") {
                result = "fail 4: $local"
                return@builder
            }
        }

        loop@for (count in 0..3) {
            konst local = B(
                    if (!condition) "1" else suspendWithArgument("#"),
                    when {
                        count > 0 -> break@loop
                        condition -> suspendWithArgument("O")
                        else -> "2"
                    },
                    if (condition) suspendHere() else suspendWithArgument("3")
            )

            if (count > 0) {
                result = "fail 5: count=$count"
                return@builder
            }

            if (local.toString() != "#OK") {
                result = "fail 5: $local"
                return@builder
            }
        }

        for (count in 0..3) {
            konst local = C(
                    1234567890123L,
                    suspendWithDouble(3.14),
                    42,
                    if (count > 0) break else suspendWithArgument("OK")
            )

            if (count > 0) {
                result = "fail 6: count=$count"
                return@builder
            }

            if (local.toString() != "1234567890123#3.14#42#OK") {
                result = "fail 6: $local"
                return@builder
            }
        }
    }

    return result
}
