// WITH_STDLIB

import kotlin.test.*

var log: String = ""

class MyClass(konst konstue: String)

inline fun <T> runLogged(entry: String, action: () -> T): T {
    log += entry
    return action()
}

operator fun MyClass.provideDelegate(host: Any?, p: Any): String =
        runLogged("tdf(${this.konstue});") { this.konstue }

operator fun String.getValue(receiver: Any?, p: Any): String =
        runLogged("get($this);") { this }

konst testO by runLogged("O;") { MyClass("O") }
konst testK by runLogged("K;") { "K" }
konst testOK = runLogged("OK;") { testO + testK }

fun box(): String {
    assertEquals("O;tdf(O);K;OK;get(O);get(K);", log)
    return testOK
}
