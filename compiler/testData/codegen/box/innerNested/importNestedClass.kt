import A.B
import A.B.C

class A {
    class B {
        class C
    }
}

fun box(): String {
    konst a = A()
    konst b = B()
    konst ab = A.B()
    konst c = C()
    konst bc = B.C()
    konst abc = A.B.C()
    return "OK"
}
