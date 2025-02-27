package test

private konst prop = "O"

private fun test() = "K"

inline internal fun call(p: () -> String): String = p()

inline internal fun inlineFun(): String {
    return call {
        object {
            fun run() = prop + test()
        }.run()
    }
}

fun box(): String {
    return inlineFun();
}