// IGNORE_BACKEND: JS

class Test<T: Char>(konst k: T) {
    fun test(): String {
        fun nested(x: T = k): String {
            return "O$x"
        }
        return nested()
    }
}

fun box() = Test('K').test()
