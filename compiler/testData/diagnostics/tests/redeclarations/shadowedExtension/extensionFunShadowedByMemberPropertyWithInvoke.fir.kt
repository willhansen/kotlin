// !DIAGNOSTICS: -UNUSED_PARAMETER

interface WithInvoke {
    operator fun invoke()
    operator fun invoke(s: String)
    operator fun <T> invoke(x: T, y: Int)
    fun invoke(i: Int)
}

interface Test1 {
    konst test1: WithInvoke
}

fun Test1.test1() {}
fun Test1.test1(s: String) {}
fun Test1.test1(i: Int) {}
fun Test1.test1(x: Any, y: Int) {}
fun <T : Number> Test1.test1(x: T, y: Int) {}