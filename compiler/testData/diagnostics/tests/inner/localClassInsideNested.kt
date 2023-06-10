// FIR_IDENTICAL
class Outer {
    class Nested {
        fun foo() {
            class Local {
                konst state = <!UNRESOLVED_REFERENCE!>outerState<!>
            }
        }
    }
    
    konst outerState = 42
}