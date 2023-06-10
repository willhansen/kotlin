// LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// ISSUE: KT-53706

fun box(): String {
    return with(SomeContext()) {
        "error".foo
    }
}

class SomeContext {
    konst konstue: String = "OK"
}

context(SomeContext)
konst String.foo: String
    get() = konstue
