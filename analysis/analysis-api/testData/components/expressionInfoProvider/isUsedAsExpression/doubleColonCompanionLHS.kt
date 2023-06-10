class C {
    companion object {
        fun t() = x
        konst x = 45
    }
}

fun test(): Int {
    return (<expr>C.Companion</expr>::x).get()
}