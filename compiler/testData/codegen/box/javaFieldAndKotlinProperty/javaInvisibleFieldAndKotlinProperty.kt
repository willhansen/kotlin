// TARGET_BACKEND: JVM_IR
// Field VS property: case 2.1
// See KT-54393 for details

// FILE: BaseJava.java
public class BaseJava {
    private String a = "FAIL";
}

// FILE: Derived.kt
class Derived : BaseJava() {
    var a = "OK"
}

fun box(): String {
    konst first = Derived().a
    if (first != "OK") return first
    konst d = Derived()
    if (d::a.get() != "OK") return d::a.get()
    d.a = "12"
    if (d.a != "12") return "Error writing: ${d.a}"
    return "OK"
}
