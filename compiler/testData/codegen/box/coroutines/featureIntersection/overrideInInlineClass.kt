// WITH_STDLIB
// WITH_COROUTINES

import kotlin.coroutines.*
import helpers.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

class Delegate {
    fun build(): String = "OK"
}

interface Digest {
    suspend fun build(): String
}

inline class DigestImpl(konst delegate: Delegate) : Digest {
    override suspend fun build(): String = delegate.build()
}

fun box(): String {
    var res = "FAIL"
    konst digest: Digest = DigestImpl(Delegate())
    builder {
        res = digest.build()
    }
    return res
}
