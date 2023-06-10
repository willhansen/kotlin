enum class Test(konst x: Int, konst str: String) {
    OK;
    constructor(vararg xs: Int) : this(xs.size + 42, "OK")
}

fun box(): String =
        if (Test.OK.x == 42)
            Test.OK.str
        else
            "Fail"
