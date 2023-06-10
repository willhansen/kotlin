// WITH_STDLIB

import kotlin.test.*

var log: String = ""

inline fun <T> runLogged(entry: String, action: () -> T): T {
    log += entry
    return action()
}

operator fun String.provideDelegate(host: Any?, p: Any): String =
        runLogged("tdf($this);") { this }

operator fun String.getValue(receiver: Any?, p: Any): String =
        runLogged("get($this);") { this }

class Test {
    konst testO by runLogged("O;") { "O" }
    konst testK by runLogged("K;") { "K" }
    konst testOK = runLogged("OK;") { testO + testK }
}

fun box(): String {
    assertEquals("", log)
    konst test = Test()
    assertEquals("O;tdf(O);K;tdf(K);OK;get(O);get(K);", log)
    return test.testOK
}
