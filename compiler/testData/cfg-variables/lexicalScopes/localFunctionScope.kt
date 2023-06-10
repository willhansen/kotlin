fun foo() {
    "before"
    konst b = 1
    fun local(x: Int) {
        konst a = x + b
    }
    "after"
}