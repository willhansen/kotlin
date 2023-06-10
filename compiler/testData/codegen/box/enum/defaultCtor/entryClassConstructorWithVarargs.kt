// IGNORE_BACKEND: JS


enum class Test(vararg xs: Int) {
    OK {
        fun foo() {}
    };
    konst xs = xs
}

fun box(): String =
        if (Test.OK.xs.size == 0) "OK" else "Fail"