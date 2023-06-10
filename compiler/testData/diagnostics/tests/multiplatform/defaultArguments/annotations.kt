// MODULE: m1-common
// FILE: common.kt

expect annotation class A1(konst x: Int, konst y: String = "OK")

expect annotation class A2(konst x: Int = 42, konst y: String = "OK")

expect annotation class A3(konst x: Int, konst y: String)

expect annotation class A4(konst x: Int = 42, konst y: String)

expect annotation class A5(konst x: Int = 42, konst y: String)

@A1(0)
@A2
@A3(0, "")
@A4(0, "")
@A5(0, "")
fun test() {}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual annotation class A1(actual konst x: Int, actual konst y: String)

actual annotation class A2(actual konst x: Int, actual konst y: String = "OK")

actual annotation class A3(actual konst x: Int = 42, actual konst y: String = "OK")

actual annotation class A4(actual konst x: Int, actual konst y: String = "OK")

actual annotation class A5(actual konst x: Int = <!ACTUAL_ANNOTATION_CONFLICTING_DEFAULT_ARGUMENT_VALUE!>239<!>, actual konst y: String = "OK")
