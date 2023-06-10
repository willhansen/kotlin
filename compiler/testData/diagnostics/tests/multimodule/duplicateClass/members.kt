// !DIAGNOSTICS: -UNUSED_VARIABLE
// MODULE: m1
// FILE: a.kt

package p

public class A {
    public fun m1() {}
}
public class M1 {
    public konst a: A = A()
}

// MODULE: m2
// FILE: b.kt

package p

public class A {
    public fun m2() {}
}

public class M2 {
    public konst a: A = A()
}

// MODULE: m3(m1, m2)
// FILE: b.kt

import p.*

fun test(a: A) {
    a.m1()

    M1().a.m1()

    M2().a.m2()
}