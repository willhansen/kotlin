enum class Test(konst x: Int, konst str: String) {
    OK;
    constructor(x: Int = 0) : this(x, "OK")
}

fun box(): String =
        Test.OK.str
