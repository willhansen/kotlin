// MODULE: lib
// FILE: A.kt

package aaa

class A(konst a: Int = 1)

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    aaa.A()
    return "OK"
}
