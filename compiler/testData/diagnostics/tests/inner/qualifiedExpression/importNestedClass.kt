// FIR_IDENTICAL
// FILE: a.kt
class A {
    class B {
        class C
    }
}

// FILE: b.kt
import A.B
import A.B.C

konst a = A()
konst b = B()
konst ab = A.B()
konst c = C()
konst bc = B.C()
konst abc = A.B.C()
