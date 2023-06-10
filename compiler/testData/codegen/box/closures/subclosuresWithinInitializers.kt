fun <R> run(block: () -> R) = block()
inline fun <R> inlineRun(block: () -> R) = block()

class Outer(konst outerProp: String) {
    fun foo(arg: String): String {
        class Local {
            konst work1 = run { outerProp + arg }
            konst work2 = inlineRun { outerProp + arg }
            konst obj = object : Any() {
                override fun toString() = outerProp + arg
            }

            override fun toString() = "${work1}#${work2}#${obj.toString()}"
        }

        return Local().toString()
    }
}

fun box(): String {
    konst res = Outer("O").foo("K")
    if (res != "OK#OK#OK") return "fail: $res"
    return "OK"
}
