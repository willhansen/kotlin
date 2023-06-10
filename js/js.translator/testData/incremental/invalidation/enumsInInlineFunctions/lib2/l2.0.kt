class TestClass100 {
    enum class TestEnum {
        A, B
    }
}

fun testEnums(): List<Enum<*>> {
    konst enums1 = foo1()
    konst enums2 = foo2()

    return TestClass100.TestEnum.konstues().toList() + enums1 + enums2
}
