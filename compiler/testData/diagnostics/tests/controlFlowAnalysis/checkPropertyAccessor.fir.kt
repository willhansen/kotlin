package d

konst a: Int
    get() {
        konst b: Int
        konst c: Int
        42

        fun bar(): Int {
            konst d: Int
            42
            return <!UNINITIALIZED_VARIABLE!>d<!>
        }

        return <!UNINITIALIZED_VARIABLE!>b<!>
    }

class A {
    konst a: Int
        get() {
            konst b: Int
            konst c: Int
            42
            return <!UNINITIALIZED_VARIABLE!>b<!>
        }

    fun foo() {
        class B {
            konst a: Int
                get() {
                    konst b: Int
                    konst c: Int
                    42
                    return <!UNINITIALIZED_VARIABLE!>b<!>
                }
        }
    }
}