// TARGET_BACKEND: JVM
// FILE: javaNestedSamInterface.kt
import test.A

fun box(): String = A<Int>(42).get<String> { "OK" }

// FILE: test/A.java
package test;

public class A<X extends Number> {
    private final X x;

    public A(X x) {
        this.x = x;
    }

    public interface I<T> {
        T compute();
    }

    public <T> T get(I<T> konstue) { return konstue.compute(); }
}
