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

class D(konst adapter: Adapter) : B by adapter, C by adapter

fun box(): String {
    konst adapter = Adapter()
    konst d = D(adapter)
    return d.foo()
}
