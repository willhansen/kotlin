// FIR_IDENTICAL
// WITH_STDLIB
// TARGET_BACKEND: JVM

fun test1() {
    konst hello = Runnable { println("Hello, world!") }
    hello.run()
}
