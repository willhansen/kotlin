// IGNORE_BACKEND_K2: JS_IR
// CHECK_CASES_COUNT: function=crash count=2 TARGET_BACKENDS=JS
// CHECK_CASES_COUNT: function=crash count=0 IGNORED_BACKENDS=JS
// CHECK_IF_COUNT: function=crash count=1 TARGET_BACKENDS=JS
// CHECK_IF_COUNT: function=crash count=3 IGNORED_BACKENDS=JS

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