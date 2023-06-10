// TARGET_BACKEND: JVM
// WITH_STDLIB
// LANGUAGE: +ValueClasses

@JvmInline
konstue class Str(konst s: String)

@JvmOverloads
fun test(so: String = "O", sk: String = "K") = Str(so + sk)

fun box(): String =
    test().s