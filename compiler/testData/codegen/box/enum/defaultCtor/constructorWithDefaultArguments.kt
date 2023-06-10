enum class Test(konst str: String = "OK") {
    OK
}

fun box(): String =
        Test.OK.str