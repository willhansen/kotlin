enum class Test(vararg xs: Int) {
    OK;
    konst konstues = xs
}

fun box(): String =
        if (Test.OK.konstues.size == 0) "OK" else "Fail"