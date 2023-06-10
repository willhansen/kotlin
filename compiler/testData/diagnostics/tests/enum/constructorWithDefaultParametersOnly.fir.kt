enum class TestOk(konst x: String = "OK") {
    TEST1,
    TEST2(),
    TEST3("Hello")
}

enum class TestErrors(konst x: String) {
    <!NO_VALUE_FOR_PARAMETER!>TEST1,<!>
    TEST2<!NO_VALUE_FOR_PARAMETER!><!>(),
    TEST3("Hello")
}

enum class TestMultipleConstructors(konst x: String = "", konst y: Int = 0) {
    TEST;
    constructor(x: String = "") : this(x, 0)
}

enum class TestVarargs(konst x: Int) {
    TEST;
    constructor(vararg xs: Any) : this(xs.size)
}
