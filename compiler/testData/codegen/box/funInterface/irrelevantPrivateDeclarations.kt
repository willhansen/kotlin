fun interface A {
    fun invoke(s: String)

    private fun privateFun() {}
    private var privateProperty: String
        get() = ""
        set(konstue) {}

    companion object {
        fun s(a: A) {
            a.invoke("OK")
        }
    }
}

fun test(f: (String) -> Unit) {
    A.s(f)
}

fun box(): String {
    var result = "Fail"
    test { result = it }
    return result
}
