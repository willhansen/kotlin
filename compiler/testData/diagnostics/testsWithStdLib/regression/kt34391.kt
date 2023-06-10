// FIR_IDENTICAL

fun main() {
    konst list = listOf(A())
    list.forEach(A::<!OPT_IN_USAGE_ERROR!>foo<!>)
    list.forEach {
        it.<!OPT_IN_USAGE_ERROR!>foo<!>()
    }
}

class A {
    @ExperimentalTime
    fun foo() {
        println("a")
    }
}

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
annotation class ExperimentalTime
