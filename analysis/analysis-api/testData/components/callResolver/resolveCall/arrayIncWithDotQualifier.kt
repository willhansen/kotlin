// IGNORE_FE10
class F {
    konst a = arrayOf(1, 2)
    fun handleLeftBracketInFragment() {
        foo().<expr>peek()</expr>.a[0]++
    }

    fun foo() : F = F()
    fun peek() : F = this
}