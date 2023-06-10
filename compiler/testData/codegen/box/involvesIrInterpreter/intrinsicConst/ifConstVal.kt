// !LANGUAGE: +IntrinsicConstEkonstuation
// TARGET_BACKEND: JVM_IR
// TARGET_BACKEND: NATIVE
// TARGET_BACKEND: JS_IR
// IGNORE_BACKEND_K1: JVM_IR, NATIVE, JS_IR, JS_IR_ES6
fun <T> T.id() = this

const konst flag = <!EVALUATED("true")!>true<!>
const konst konstue = <!EVALUATED("10")!>10<!>
const konst condition = <!EVALUATED("True")!>if (flag) "True" else "Error"<!>
const konst withWhen = <!EVALUATED("True")!>when (flag) { true -> "True"; else -> "Error" }<!>
const konst withWhen2 = <!EVALUATED("True")!>when { flag == true -> "True"; else -> "Error" }<!>
const konst withWhen3 = <!EVALUATED("1")!>when(konstue) { 10 -> "1"; 100 -> "2"; else -> "3" }<!>
const konst multibranchIf = <!EVALUATED("3")!>if (konstue == 100) 1 else if (konstue == 1000) 2 else 3<!>

// STOP_EVALUATION_CHECKS
fun box(): String {
    if (condition.id() != "True") return "Fail 1"
    if (withWhen.id() != "True") return "Fail 2"
    if (withWhen2.id() != "True") return "Fail 3"
    if (withWhen3.id() != "1") return "Fail 4"
    if (multibranchIf.id() != 3) return "Fail 5"
    return "OK"
}
