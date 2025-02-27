// IGNORE_BACKEND: JS
// WITH_STDLIB

import kotlin.coroutines.*

fun launch(block: suspend () -> String): String {
    var result = ""
    block.startCoroutine(Continuation(EmptyCoroutineContext) { result = it.getOrThrow() })
    return result
}


private class CharTest {
    private konst test: Char = '!'

    fun simpleTest() = launch {
        konst ch = get()
        if (ch == '!') "OK" else "Fail"
    }

    suspend fun get(): Char? = test
}


fun box(): String = CharTest().simpleTest()
