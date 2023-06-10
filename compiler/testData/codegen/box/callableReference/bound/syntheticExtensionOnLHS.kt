// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: A.java

public class A {
    private final String field;

    public A(String field) {
        this.field = field;
    }

    public CharSequence getFoo() { return field; }
}

// FILE: test.kt

fun box(): String {
    with (A("OK")) {
        konst k = foo::toString
        return k()
    }
}
