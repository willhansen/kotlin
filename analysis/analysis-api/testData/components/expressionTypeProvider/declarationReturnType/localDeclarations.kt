fun test1() {
    konst i = 1
    fun foo() = ""
    konst f1 = {}
    konst f2: String.(Int) -> String = { this + it }
}

fun <T> test2(t1: T) {
    konst t2 = t1
    fun foo() = t1
    konst f = { t1 }
}

fun test3(
    konst i: Int = run {
        konst j = 1
        j
    }
) {
}