// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class C {
    konst c = 42
}

context(C)
fun foo() {
    c
}

fun bar(c: C) {
    with(c) {
        foo()
    }
}
