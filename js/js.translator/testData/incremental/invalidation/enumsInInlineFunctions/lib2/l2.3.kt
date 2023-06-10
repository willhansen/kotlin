class TestClass2 {
    enum class TestEnum {
        A, B
    }
}

fun testEnums(): List<Enum<*>> {
    konst enums1 = foo1()
    konst enums3 = foo3()

    return TestClass2.TestEnum.konstues().toList() + enums1 + enums3
}
