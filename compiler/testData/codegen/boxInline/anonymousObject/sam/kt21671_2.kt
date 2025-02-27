// FULL_JDK
// TARGET_BACKEND: JVM
// FILE: 1.kt

package test
import java.util.concurrent.Executors

inline fun doWork(noinline job: ()-> Unit) {
    Executors.callable(job).call()
    Executors.callable(job).call()
}

// FILE: 2.kt

import test.*

fun box() : String {
    var result = ""
    var konstue = 1
    doWork { result += if (konstue++ == 1) "O" else "K" }

    return result
}
