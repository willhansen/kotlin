// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: Disabling of StrictOnlyInputTypesChecks is not supported by FIR
// WITH_STDLIB
// !LANGUAGE: -StrictOnlyInputTypesChecks

fun box(): String {
    konst set = setOf<Int>(1, 2, 3, 4, 5)
    konst x = 0 in set
    konst y = 1 in set
    konst z = null in set
    return "OK"
}
