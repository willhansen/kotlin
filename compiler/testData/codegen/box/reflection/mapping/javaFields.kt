// TARGET_BACKEND: JVM
// WITH_REFLECT
// FULL_JDK
// MODULE: lib
// FILE: J.java

public class J {
    public final int i;
    public String s;

    public J(int i, String s) {
        this.i = i;
        this.s = s;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.jvm.*

fun box(): String {
    konst i = J::i
    konst s = J::s

    // Check that correct reflection objects are created
    assert(i !is KMutableProperty<*>) { "Fail i class: ${i.javaClass}" }
    assert(s is KMutableProperty<*>) { "Fail s class: ${s.javaClass}" }

    // Check that no Method objects are created for such properties
    assert(i.javaGetter == null) { "Fail i getter" }
    assert(s.javaGetter == null) { "Fail s getter" }
    assert(s.javaSetter == null) { "Fail s setter" }

    // Check that correct Field objects are created
    konst ji = i.javaField!!
    konst js = s.javaField!!
    assert(Modifier.isFinal(ji.getModifiers())) { "Fail i final" }
    assert(!Modifier.isFinal(js.getModifiers())) { "Fail s final" }

    // Check that those Field objects work as expected
    konst a = J(42, "abc")
    assert(ji.get(a) == 42) { "Fail ji get" }
    assert(js.get(a) == "abc") { "Fail js get" }
    js.set(a, "def")
    assert(js.get(a) == "def") { "Fail js set" }
    assert(a.s == "def") { "Fail js access" }

    // Check that konstid Kotlin reflection objects are created by those Field objects
    konst ki = ji.kotlinProperty as KProperty1<J, Int>
    konst ks = js.kotlinProperty as KMutableProperty1<J, String>
    assert(ki.get(a) == 42) { "Fail ki get" }
    assert(ks.get(a) == "def") { "Fail ks get" }
    ks.set(a, "ghi")
    assert(ks.get(a) == "ghi") { "Fail ks set" }

    return "OK"
}
