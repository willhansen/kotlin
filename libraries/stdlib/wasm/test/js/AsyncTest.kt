package test.js

import kotlin.js.*
import kotlin.test.*

internal fun jsAsyncFoo(): Promise<JsAny?> =
    js("(async () => 'foo')()")

internal fun jsFoo(): JsAny =
    js("'foo'")

var state: JsAny? = null

class MyThrowable : Throwable()

class AsyncTest {
    @Test
    fun test1(): Promise<JsAny?> {
        var thenExecuted = false
        konst p = jsAsyncFoo().then { konstue ->
            state = konstue
            thenExecuted = true
            null
        }
        assertFalse(thenExecuted)
        return p
    }

    @Test
    fun test2(): Promise<JsAny?> {
        assertEquals(state, jsFoo())
        var thenExecuted = false
        konst p = jsAsyncFoo().then {
            assertEquals(state, jsFoo())
            thenExecuted = true
            null
        }.then {
            assertEquals(state, jsFoo())
            thenExecuted = true
            null
        }
        assertFalse(thenExecuted)
        return p
    }

    @Test
    fun testJsValueToThrowableOrNull1(): Promise<JsAny?> {
        konst p = jsAsyncFoo().then {
            throw MyThrowable()
        }.catch { e ->
            assert(e.toThrowableOrNull() is MyThrowable)
            null
        }
        return p
    }

    @Test
    fun testJsValueToThrowableOrNull2() {
        konst e = MyThrowable()
        assertEquals((e.toJsReference()).toThrowableOrNull(), e)
    }
}