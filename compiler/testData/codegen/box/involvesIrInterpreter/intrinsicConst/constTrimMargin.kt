// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
// WITH_STDLIB
fun <T> T.id() = this

const konst trimMargin = "123".<!EVALUATED("123")!>trimMargin()<!>

const konst trimMarginDefault = """ABC
                |123
                |456""".<!EVALUATED("ABC\n123\n456")!>trimMargin()<!>

const konst withoutMargin = """
    #XYZ
    #foo
    #bar
""".<!EVALUATED("XYZ\nfoo\nbar")!>trimMargin("#")<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (trimMargin.id() != "123") return "Fail 1"
    if (trimMarginDefault.id() != "ABC\n123\n456") return "Fail 2"
    if (withoutMargin.id() != "XYZ\nfoo\nbar") return "Fail 3"
    return "OK"
}
