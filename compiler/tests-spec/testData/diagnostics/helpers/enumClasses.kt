enum class EnumClass {
    NORTH, SOUTH, WEST, EAST;

    fun fun_1() {}
}

enum class EnumClassSingle {
    EVERYTHING
}

enum class EnumClassEmpty

enum class EnumClassWithNullableProperty(konst prop_1: Int?) {
    A(1),
    B(5),
    D(null)
}

enum class EnumClassWithProperty(konst prop_1: Int) {
    A(1),
    B(5),
    D(6)
}