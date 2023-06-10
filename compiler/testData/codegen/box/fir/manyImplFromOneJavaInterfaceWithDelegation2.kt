// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// FILE: A.java

public interface A {
    default String foo() {
        return "OK";
    }
}

// FILE: B.java

public interface B extends A {}

// FILE: C.java

public interface C extends A {}

// FILE: test.kt

class Adapter : B, C

class D(konst b: B, konst c: C) : B by b, C by c

fun box(): String {
    konst b = Adapter()
    konst c = Adapter()
    konst d = D(b, c)
    return d.foo()
}
