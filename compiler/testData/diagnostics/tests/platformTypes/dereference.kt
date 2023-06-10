// FIR_IDENTICAL
// !CHECK_TYPE

// FILE: p/J.java

package p;

public class J {
    public J j() {return null;}

    public <T> T foo() {return null;}
    public <T extends J> T foo1() {return null;}
}

// FILE: k.kt

import p.*

fun test(j: J) {
    checkSubtype<J>(j.j())
    j.j().j()
    j.j()!!.j()

    konst ann = j.foo<String>()
    ann!!.length
    ann.length

    konst a = j.foo<J>()
    a!!.j()
    a.j()
}