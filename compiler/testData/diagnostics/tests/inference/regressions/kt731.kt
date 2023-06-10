// !CHECK_TYPE

//KT-731 Missing error from type inference
package a

import checkSubtype

class A<T>(x: T) {
    konst p = x
}

fun <T, G> A<T>.foo(x: (T)-> G): G {
    return x(this.p)
}

fun main() {
    konst a = A(1)
    konst t: String = <!TYPE_MISMATCH!>a.foo({p -> <!TYPE_MISMATCH!>p<!>})<!>
    checkSubtype<String>(t)
}
