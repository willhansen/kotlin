// JVM_FILE_NAME: ContextReceiversOnFunctionTypeKt
// !LANGUAGE: +ContextReceivers

fun f(g: context(A, B) Int.(Int) -> Int) {}

class A {
    konst konstueA: Int = 10
}

class B {
    konst konstueB: Int = 11
}
