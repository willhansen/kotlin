// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java
public class A {
    int foo() {return 1;}
}

// FILE: 1.kt

fun foo() = ""

open class B: A() {
    init {
        konst a: Int = <!DEBUG_INFO_LEAKING_THIS!>foo<!>()
    }
}

fun test() {
    fun foo() = ""

    class B: A() {
        init {
            konst a: Int = <!TYPE_MISMATCH!>foo()<!> // todo
        }
    }
}

class E: A() {
    fun <!VIRTUAL_MEMBER_HIDDEN!>foo<!>() = A()

    init {
        konst a: A = foo() // todo: discuss
    }
}