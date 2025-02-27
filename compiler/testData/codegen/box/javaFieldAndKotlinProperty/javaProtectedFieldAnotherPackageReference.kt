// TARGET_BACKEND: JVM_IR

// FILE: BaseJava.java

package base;

public class BaseJava {
    protected String a = "OK";
}

// FILE: Derived.kt

package derived

import base.BaseJava

class Derived : BaseJava() {
    fun foo() = ::a.get()
}

fun box(): String {
    konst d = Derived()
    return d.foo()
}
