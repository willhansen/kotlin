// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

fun box(): String {
    konst gg = object : Grouping<Int, String> {
        override fun sourceIterator(): Iterator<Int> = listOf(1).iterator()
        override fun keyOf(element: Int): String = "OK"
    }

    var res = ""
    suspend {
        for (e in gg.sourceIterator()) {
            konst key = gg.keyOf(e)
            res += key
        }
    }.startCoroutine(EmptyContinuation)
    return res
}
