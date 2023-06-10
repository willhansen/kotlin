// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// WITH_COROUTINES
// WITH_STDLIB
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class BoxAny(konst konstue: Any?) {
    konst intValue: Int get() = konstue as Int
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class BoxInt(konst konstue: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class BoxLong(konst konstue: Long)

class EmptyContinuation<T> : Continuation<T> {
    override konst context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>) {}
}

suspend fun foo(block: suspend (BoxAny) -> Unit) {
    block(BoxAny(1))
    block.startCoroutineUninterceptedOrReturn(BoxAny(1), EmptyContinuation())
}

suspend fun fooReceiver(block: suspend BoxAny.() -> Unit) {
    BoxAny(1).block()
    block.startCoroutineUninterceptedOrReturn(BoxAny(1), EmptyContinuation())
}

suspend fun bar(block: suspend (BoxInt) -> Unit) {
    block(BoxInt(2))
    block.startCoroutineUninterceptedOrReturn(BoxInt(2), EmptyContinuation())
}

suspend fun barReceiver(block: suspend BoxInt.() -> Unit) {
    BoxInt(2).block()
    block.startCoroutineUninterceptedOrReturn(BoxInt(2), EmptyContinuation())
}

suspend fun baz(block: suspend (BoxLong) -> Unit) {
    block(BoxLong(3))
    block.startCoroutineUninterceptedOrReturn(BoxLong(3), EmptyContinuation())
}

suspend fun bazReceiver(block: suspend BoxLong.() -> Unit) {
    BoxLong(3).block()
    block.startCoroutineUninterceptedOrReturn(BoxLong(3), EmptyContinuation())
}

suspend fun BoxAny.extension(block: suspend BoxAny.() -> Unit) {
    this.block()
    block()

    block.startCoroutineUninterceptedOrReturn(this, EmptyContinuation())
}

suspend fun BoxInt.extension(block: suspend BoxInt.() -> Unit) {
    this.block()
    block()

    block.startCoroutineUninterceptedOrReturn(this, EmptyContinuation())
}

suspend fun BoxLong.extension(block: suspend BoxLong.() -> Unit) {
    this.block()
    block()

    block.startCoroutineUninterceptedOrReturn(this, EmptyContinuation())
}

fun runBlocking(block: suspend () -> Unit) {
    block.startCoroutine(object : Continuation<Unit> {
        override konst context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            (block as Function1<Continuation<Unit>, Any?>)(this)
        }
    })
}

fun box(): String {
    var result = 0
    runBlocking {
        foo { boxAny ->
            result += boxAny.intValue
        }
        fooReceiver {
            result += this.intValue
        }

        bar { boxInt ->
            result += boxInt.konstue
        }
        barReceiver {
            result += konstue
        }

        baz { boxLong ->
            result += boxLong.konstue.toInt()
        }
        bazReceiver {
            result += this.konstue.toInt()
        }

        konst b = BoxAny(4)
        b.extension {
            result += intValue
        }

        konst bInt = BoxInt(5)
        BoxInt(5).extension {
            result += konstue + bInt.konstue
        }

        BoxLong(6).extension {
            result += konstue.toInt()
        }
    }

    return if (result == 168) "OK" else "Error: $result"
}