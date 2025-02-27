data class A(konst x: String, konst y: String)

suspend inline fun foo(a: A, block: suspend (A) -> String): String = block(a)

// FILE: test.kt
suspend fun test() = foo(A("O", "K")) { (x_param, y_param) -> x_param + y_param }

// @TestKt.class:
// 1 LOCALVARIABLE x_param Ljava/lang/String;
// 1 LOCALVARIABLE y_param Ljava/lang/String;