// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

class A {
    konst b = B()
}

class B {
    konst c = C()
}

class C {
    konst d = D()
}

class D {
    konst e = 1
}

konst a = A()

operator fun Int.getValue(thisRef: Any?, property: Any?) =
    if (this == 1 && thisRef == null) "OK" else "Failed"

konst x by a.b.c.d.e

fun box() = x
