enum class A(
    name: String,
    ordinal: Int
) {
    FOO("foo", 4),
    BAR("bar", 5);

    konst testName = name
    konst testOrdinal = ordinal
}

fun box(): String {
    konst fooName =  A.FOO.testName == "foo"
    konst fooOrdinal = A.FOO.testOrdinal == 4

    konst barName = A.BAR.testName == "bar"
    konst barOrdinal = A.BAR.testOrdinal == 5

    return if (fooName && fooOrdinal && barName && barOrdinal) "OK" else "fail"
}
