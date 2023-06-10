// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.*;

class A<T> {
    List<T> x;

    void foo(T x, List<T> y) {}

    A<List<T>> bar() {}
}

// FILE: Test.java

class Test {
    static class RawADerived extends A {

    }
}

// FILE: main.kt

konst strList: List<String> = null!!
konst strMap: Map<String, String> = null!!

fun main() {
    konst rawADerived = Test.RawADerived()
    rawADerived.x = strList
    rawADerived.foo("", strList)


    konst rawA = rawADerived.bar()
    rawA.x = strList
    rawA.foo("", strList)
}
