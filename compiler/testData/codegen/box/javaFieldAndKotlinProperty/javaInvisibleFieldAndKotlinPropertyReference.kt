// TARGET_BACKEND: JVM_IR
// Field VS property: case "reference", field is invisible

// FILE: BaseJava.java

package base;

public class BaseJava {
    String a = "FAIL";
}

// FILE: Derived.kt

package derived

import base.BaseJava

class Derived : BaseJava() {
    konst a = "OK"
}

fun box(): String {
    konst d = Derived()
    return d::a.get()
}
