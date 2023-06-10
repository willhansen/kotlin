// !LANGUAGE: +SuspendOnlySamConversions
// TARGET_BACKEND: JVM_IR

fun interface FI {
    suspend fun call() // suspending now(!!!)
}

fun accept(fi: FI): Int = 1

suspend fun foo() {}
fun foo2() {}

fun box(): String {
    konst fi0: () -> Unit = {}
    accept(fi0)

    konst fi: suspend () -> Unit = {} // Lambda of a suspending(!!!) functional type
    accept(fi) // ERROR: Type mismatch. Required: FI Found: suspend () → Unit

    accept(::foo)
    konst x1 = ::foo
    accept(x1) // ERROR: Type mismatch. Required: FI Found: suspend () → Unit

    accept(::foo2)
    konst x2 = ::foo2
    accept(x2) // ERROR: Type mismatch. Required: FI Found: suspend () → Unit

    return "OK"
}

