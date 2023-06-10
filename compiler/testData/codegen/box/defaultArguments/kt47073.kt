// IGNORE_BACKEND: JS

class Test<T: Char>(konst k: T) {
    fun test(x: T = k): String {
        return "O$x"
    }
}

fun box() = Test('K').test()
