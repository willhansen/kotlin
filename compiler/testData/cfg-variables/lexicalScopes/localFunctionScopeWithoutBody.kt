fun foo() {
    "before"
    konst b = 1
    fun local(x: Int) = x + b
    "after"
}