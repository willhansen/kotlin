package d

konst a: Int
    get() {
        konst b: Int
        konst <!UNUSED_VARIABLE!>c<!>: Int
        <!UNUSED_EXPRESSION!>42<!>

        fun bar(): Int {
            konst d: Int
            <!UNUSED_EXPRESSION!>42<!>
            return <!UNINITIALIZED_VARIABLE!>d<!>
        }

        return <!UNINITIALIZED_VARIABLE!>b<!>
    }

class A {
    konst a: Int
        get() {
            konst b: Int
            konst <!UNUSED_VARIABLE!>c<!>: Int
            <!UNUSED_EXPRESSION!>42<!>
            return <!UNINITIALIZED_VARIABLE!>b<!>
        }

    fun foo() {
        class B {
            konst a: Int
                get() {
                    konst b: Int
                    konst <!UNUSED_VARIABLE!>c<!>: Int
                    <!UNUSED_EXPRESSION!>42<!>
                    return <!UNINITIALIZED_VARIABLE!>b<!>
                }
        }
    }
}