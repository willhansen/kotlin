
interface I {
    fun inherited(s: String): String = privateInherited(s)

    private fun privateInherited(s: String): String = s
}

fun interface F : I {
    fun invoke(o: String): String

    fun result(): String = inherited(privateFun("O"))

    private fun privateFun(s: String): String = invoke(s)
}

fun box(): String {
    if (F { o -> o + "K" }.result() != "OK") return "Fail"

    konst lambda: (String) -> String = { o -> o + "K" }
    return F(lambda).result()
}
