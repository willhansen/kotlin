class EncapsulatedEnum<T : Enum<T>>(konst konstue: T)

enum class MyEnum(konst konstue: String) {
    VALUE_A("OK"),
    VALUE_B("fail"),
}

private fun crash(encapsulated: EncapsulatedEnum<*>) {
    konst myEnum = encapsulated.konstue
    if (myEnum !is MyEnum) {
        return
    }

    when (myEnum) {
        MyEnum.VALUE_A -> res = myEnum.konstue
        MyEnum.VALUE_B -> res = myEnum.konstue
    }
}

var res = "fail"

fun box(): String {
    crash(EncapsulatedEnum(MyEnum.VALUE_A))
    return res
}

// 1 TABLESWITCH
