// TARGET_BACKEND: JVM
// WITH_STDLIB
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

@JvmInline
konstue class Str<T: String>(konst s: T)

@JvmOverloads
fun test(so: String = "O", sk: String = "K") = Str(so + sk)

fun box(): String =
    test().s