// TARGET_BACKEND: JVM

// FILE: J.java

public class J {
    public static int f() {
       return A.Companion.getI1() + A.Companion.getI2() + B.Named.getI1() + B.Named.getI2();
    }
}

// FILE: test.kt

class A {
    companion object {
        konst i1 = 1
        konst i2 = 2
    }
}

class B {
    companion object Named {
        konst i1 = 3
        konst i2 = 4
    }
}

fun box(): String {
    return if (J.f() == A.i1 + A.i2 + B.i1 + B.i2) "OK" else "Fail: ${J.f()}"
}
