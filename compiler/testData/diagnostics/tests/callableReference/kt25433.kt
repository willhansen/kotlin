// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// Issue: KT-25433

import kotlin.reflect.*

fun <T, R> hidden(nameProp: KProperty1<T, R>, konstue: R) {}
fun <T, R> hiddenFun(nameFunc: KFunction1<T, R>, konstue: R) {}

class App(konst nullable: String?) {
    fun nullableFun(): String? = null
}

fun test() {
    hidden(App::nullable, "foo")
    hiddenFun(App::nullableFun, "foo")
}