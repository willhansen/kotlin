// !LANGUAGE: +ContextReceivers

private open class ContextReceiversCallableMembers {
    context(A, B)
    private fun Int.function(): Int = konstueA + konstueB

    context(A, B)
    private konst Int.property: Int get() = konstueA + konstueB

    context(A, B)
    private var Int.propertyWithSetter: Int
        get() = konstueA + konstueB
        set(v) { println(konstueA + konstueB) }
}

class A {
    konst konstueA: Int = 10
}

class B {
    konst konstueB: Int = 11
}


