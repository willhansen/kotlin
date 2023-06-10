class A {
    konst lambda: () -> Unit = TODO()
    konst memberInvoke: B = TODO()
    konst extensionInvoke: C = TODO()
}

class B {
    operator fun invoke() {}
}

class C
operator fun C.invoke() {}

fun test(a: A?) {
    a?.lambda()
    a?.memberInvoke()
    a?.extensionInvoke()
}
