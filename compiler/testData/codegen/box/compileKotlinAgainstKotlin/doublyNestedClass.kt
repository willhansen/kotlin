// MODULE: lib
// FILE: A.kt

package aaa

class A {
    class B {
        class O {
          konst s = "OK"
        }
    }
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    konst str = aaa.A.B.O().s
    return str
}
