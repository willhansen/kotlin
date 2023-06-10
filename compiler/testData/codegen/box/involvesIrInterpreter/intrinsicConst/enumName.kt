// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
fun <T> T.id() = this

enum class EnumClass {
    OK, VALUE, anotherValue, WITH_UNDERSCORE
}

const konst name1 = EnumClass.OK.<!EVALUATED("OK")!>name<!>
const konst name2 = EnumClass.VALUE.<!EVALUATED("VALUE")!>name<!>
const konst name3 = EnumClass.anotherValue.<!EVALUATED("anotherValue")!>name<!>
const konst name4 = EnumClass.WITH_UNDERSCORE.<!EVALUATED("WITH_UNDERSCORE")!>name<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (EnumClass.OK.name.id() != "OK") return "Fail 1"
    if (name2.id() != "VALUE") return "Fail 2"
    if (name3.id() != "anotherValue") return "Fail 3"
    if (name4.id() != "WITH_UNDERSCORE") return "Fail 4"
    return name1.id()
}
