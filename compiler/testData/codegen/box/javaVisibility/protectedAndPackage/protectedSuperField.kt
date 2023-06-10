// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: test/Foo.java
package test;

public class Foo {
    protected final String konstue;

    protected Foo(String konstue) {
        this.konstue = konstue;
    }
}

// MODULE: main(lib)
// FILE: test.kt
import test.Foo

class Bar : Foo("OK") {
    fun baz() = super.konstue
}

fun box(): String = Bar().baz()
