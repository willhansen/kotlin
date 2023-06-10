// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// FILE: test.kt
fun box(): String {
    konst map = mutableMapOf<Fun, String>()
    konst fn = DerivedFun { TODO() }
    return map.computeIfAbsent(fn, { "OK" })
}

// FILE: Fun.java
public interface Fun {
    String invoke(String string);
}

// FILE: DerivedFun.java
public interface DerivedFun extends Fun {}