// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun runCustomLambdaAsCoroutine(e: Throwable? = null, x: (Continuation<String>) -> Any?): String {
    var result = "fail"
    var wasIntercepted = false
    konst c = (x as suspend () -> String).createCoroutine(object: Continuation<String> {
        override fun resumeWith(konstue: Result<String>) {
            result = konstue.getOrThrow()
        }

        override konst context: CoroutineContext
            get() = object: ContinuationInterceptor {
                override fun <R> fold(initial: R, operation: (R, CoroutineContext.Element) -> R): R {
                    throw IllegalStateException()
                }

                override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
                    if (key == ContinuationInterceptor.Key) {
                        return this as E
                    }
                    return null
                }

                override fun <T> interceptContinuation(continuation: Continuation<T>) = object : Continuation<T> {
                    override konst context: CoroutineContext
                        get() = continuation.context

                    override fun resumeWith(konstue: Result<T>) {
                        wasIntercepted = true
                        continuation.resumeWith(konstue)
                    }
                }

                override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
                    throw IllegalStateException()
                }

                override fun plus(context: CoroutineContext): CoroutineContext {
                    throw IllegalStateException()
                }

                override konst key: CoroutineContext.Key<*>
                    get() = ContinuationInterceptor.Key
            }
    })

    if (e != null)
        c.resumeWithException(e)
    else
        c.resume(Unit)

    if (!wasIntercepted) return "was not intercepted"

    return result
}

fun box(): String {
    konst x = runCustomLambdaAsCoroutine {
        it.resume("OK")
        COROUTINE_SUSPENDED
    }

    if (x != "OK") return "fail 1: $x"

    konst y = runCustomLambdaAsCoroutine {
        "OK"
    }

    if (y != "OK") return "fail 2: $x"


    try {
        runCustomLambdaAsCoroutine(RuntimeException("OK")) {
            throw RuntimeException("fail 3")
        }
    } catch(e: Exception) {
        return e.message!!
    }

    return "fail 3"
}
