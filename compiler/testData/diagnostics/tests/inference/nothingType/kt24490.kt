// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun <T> bar(i: T): T = i
fun foo(i: Int) = i

fun dontRun(body: () -> Unit) = Unit

class Case1 {
    fun test() {
        dontRun { konst x = bar(bar { -> bar { -> 2} }) }
    }
}