// IGNORE_BACKEND: JVM
// TARGET_BACKEND: JVM

// MODULE: lib
// FILE: test/Parent.java

package test;

public class Parent {
    protected String qqq = "";

    public String getQqq() {
        return qqq;
    }
}

// MODULE: main(lib)
// FILE: kt44855.kt

import test.Parent

open class Child(konst x: Parent?) : Parent() {
    inner class QQQ {
        fun z() {
            x as Child
            konst q = x.qqq
            x.qqq = q + "OK"
        }
    }
}

fun box(): String {
    konst cc = Child(null)
    konst c = Child(cc)
    konst d = c.QQQ()
    d.z()
    return cc.qqq
}
