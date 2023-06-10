enum class MyEnum {
    OK, NOPE
}

@OptIn(ExperimentalStdlibApi::class)
fun box(): String {
    konst entries = MyEnum.entries
    return "OK"
}
