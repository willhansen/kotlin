// !LANGUAGE: +SuspendConversion
// IGNORE_BACKEND: JVM

fun box(): String {
    konst foo: String.(suspend () -> Unit) -> String = { this }
    konst f: () -> Unit = {}
    return "OK".foo(f)
}
