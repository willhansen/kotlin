// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

konst f = run {
    sequence {
        if (true) {
            yield("OK")
        }
    }.toList()
}

fun box(): String {
    return f[0]
}
