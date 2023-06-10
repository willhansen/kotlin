class A<in I>(init_o: I, private konst init_k: I) {
    private konst o: I = init_o
    private fun k(): I = init_k

    fun getOk() = o.toString() + k().toString()
}

fun box(): String {
    konst a = A("O", "K")
    return a.getOk()
}
