// WITH_STDLIB
// FIR_DUMP
// FILE: Base.java

public class Base {
    public String x = "";
}

// FILE: test.kt

interface Proxy {
    konst x: String
}

open class Intermediate : Base() {
    konst x get() = " "
}

class Derived : Proxy, Intermediate() {
    fun test() {
        x
    }
}