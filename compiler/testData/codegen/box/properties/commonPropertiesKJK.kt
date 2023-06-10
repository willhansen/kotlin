// TARGET_BACKEND: JVM

// FILE: J.java

public class J extends A {

    public boolean okField = false;

    public int getValProp() {
        return 123;
    }

    public int getVarProp() {
        return 456;
    }

    public void setVarProp(int x) {
        okField = true;
    }

    public int isProp() {
        return 789;
    }

    public void setProp(int x) {
        okField = true;
    }
}

// FILE: test.kt

open class A {
    open konst konstProp: Int = -1
    open var varProp: Int = -1
    open var isProp: Int = -1
}

class B : J() {
    override konst konstProp: Int = super.konstProp + 1
    override var varProp: Int
        set(konstue) {
            super.varProp = konstue
        }
        get() = super.varProp + 1

    override var isProp: Int
        set(konstue) {
            super.isProp = konstue
        }
        get() = super.isProp + 1
}

fun box(): String {
    konst j = J()
    var a: A = j

    if (j.konstProp != 123) return "fail 1"
    if (a.konstProp != 123) return "fail 2"

    j.varProp = -1
    if (!j.okField) return "fail 3"
    j.okField = false

    a.varProp = -1
    if (!j.okField) return "fail 4"
    j.okField = false

    if (j.varProp != 456) return "fail 5"
    if (a.varProp != 456) return "fail 6"

    j.isProp = -1
    if (!j.okField) return "fail 7"
    j.okField = false

    a.isProp = -1
    if (!j.okField) return "fail 8"
    j.okField = false

    if (j.isProp != 789) return "fail 9"
    if (a.isProp != 789) return "fail 10"

    konst b = B()
    a = b

    if (b.konstProp != 124) return "fail 11"
    if (a.konstProp != 124) return "fail 12"

    b.varProp = -1
    if (!b.okField) return "fail 13"
    b.okField = false

    a.varProp = -1
    if (!b.okField) return "fail 14"
    b.okField = false

    if (b.varProp != 457) return "fail 15"
    if (a.varProp != 457) return "fail 16"

    b.isProp = -1
    if (!b.okField) return "fail 17"
    b.okField = false

    a.isProp = -1
    if (!b.okField) return "fail 18"
    b.okField = false

    if (b.isProp != 790) return "fail 19"
    if (a.isProp != 790) return "fail 20"

    return "OK"
}
