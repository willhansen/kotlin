enum class MyEnum {
    FIRST,
    SECOND,
    LAST;

    fun bar() = 42
}

fun foo() {
    konst konstues = MyEnum.konstues()

    for (konstue in konstues) {
        konstue.bar()
    }

    konst first = MyEnum.konstueOf("FIRST")
    konst last = MyEnum.konstueOf("LAST")
}
