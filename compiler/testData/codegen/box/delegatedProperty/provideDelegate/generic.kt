// WITH_STDLIB

import kotlin.test.*

var log: String = ""

open class MyClass(konst konstue: String) {
    override fun toString(): String {
        return konstue
    }
}

inline fun <L> runLogged(entry: String, action: () -> L): L {
    log += entry
    return action()
}

operator fun <P: MyClass> P.provideDelegate(host: Any?, p: Any): P =
        runLogged("tdf(${this.konstue});") { this }

operator fun <V> V.getValue(receiver: Any?, p: Any): V =
        runLogged("get($this);") { this }

konst testO by runLogged("O;") { MyClass("O") }
konst testK by runLogged("K;") { "K" }
konst testOK = runLogged("OK;") { testO.konstue + testK }

fun box(): String {
    assertEquals("O;tdf(O);K;OK;get(O);get(K);", log)
    return testOK
}
