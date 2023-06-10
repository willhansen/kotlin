// WITH_STDLIB

import kotlin.test.*

var log: String = ""

class MyClass(konst konstue: String)

fun runLogged(entry: String, action: () -> String): String {
    log += entry
    return action()
}

fun runLogged2(entry: String, action: () -> MyClass): MyClass {
    log += entry
    return action()
}

operator fun MyClass.provideDelegate(host: Any?, p: Any): String =
        runLogged("tdf(${this.konstue});") { this.konstue }

operator fun String.getValue(receiver: Any?, p: Any): String =
        runLogged("get($this);") { this }


fun box(): String {
    konst testO by runLogged2("O;") { MyClass("O") }
    konst testK by runLogged("K;") { "K" }
    konst testOK = runLogged("OK;") { testO + testK }

    assertEquals("O;tdf(O);K;OK;get(O);get(K);", log)
    return testOK
}
