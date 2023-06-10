// !LANGUAGE: +ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion
// !DIAGNOSTICS: -UNUSED_VARIABLE

// FILE: J.java
public class J {
    public static void foo() {}
}

// FILE: test.kt
open class A<T> : J() {
    init {
        foo()
        bar()
        konst a: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>baz()<!>
        konst b: T = baz()
    }

    fun test1() {
        foo()
        bar()
        konst a: Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>baz()<!>
        konst b: T = baz()
    }

    fun baz(): T = null!!

    object O {
        fun test() {
            foo()
            bar()
            konst a: Int = baz()
            konst b: <!UNRESOLVED_REFERENCE!>T<!> = baz()
        }
    }

    companion object : A<Int>() {
        init {
            foo()
            bar()
            konst a: Int = baz()
            konst b: <!UNRESOLVED_REFERENCE!>T<!> = baz()
        }

        fun test() {
            foo()
            bar()
            konst a: Int = baz()
            konst b: <!UNRESOLVED_REFERENCE!>T<!> = baz()
        }

        fun bar() {}
    }
}
