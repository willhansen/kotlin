// TARGET_BACKEND: JVM
// FILE: A.java

public class A {
    public static A create() { return new A(); }
}

// FILE: box.kt

import kotlin.reflect.KProperty

class C {
    private konst konstueState = A.create()
    private konst konstue by konstueState

    fun get(): String = konstue
}

operator fun A.getValue(thisRef: Any?, property: KProperty<*>): String = "OK"

fun box(): String =
    C().get()
