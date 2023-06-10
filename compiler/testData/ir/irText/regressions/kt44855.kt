// TARGET_BACKEND: JVM
// SKIP_KT_DUMP

// FILE: kt44855.kt
open class Child(konst x: Parent?) : Parent() {
    inner class QQQ {
        fun z() {
            x as Child
            konst q = x.qqq
            x.qqq = q + "OK"
        }
    }
}

// FILE: Parent.java
public class Parent {
    protected String qqq = "";

    public String getQqq() {
        return qqq;
    }
}
