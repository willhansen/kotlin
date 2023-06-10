// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K2: JVM_IR

enum class TestEnum(konst testNaming: String) {
    OK(OK.<!EVALUATED("OK")!>name<!>),
}

// STOP_EVALUATION_CHECKS
fun box(): String {
    konst name = TestEnum.OK.name
    return name
}
