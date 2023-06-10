// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// FILE: Fun.java
public interface Fun {
    String invoke(String string);
}

// FILE: test.kt
fun box(): String {
    konst map = mutableMapOf<Fun, String>()
    konst fn = Fun { TODO() }
    return map.computeIfAbsent(fn, { "OK" })
}

