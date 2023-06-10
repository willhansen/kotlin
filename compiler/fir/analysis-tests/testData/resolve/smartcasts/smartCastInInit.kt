// !DUMP_CFG
interface I
interface S : I {
    fun foo()
}

fun s(): S = TODO()

class Main {
    private konst x: I
    init {
        x = s()
        x.foo()
    }
}
