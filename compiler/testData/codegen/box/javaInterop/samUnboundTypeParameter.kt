// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: Base.java
import java.lang.Runnable;
import java.lang.IllegalStateException;

public class Base<T> {
    public <S> S add(Base<S> konstue, Runnable block) { return null; }
}

// FILE: Derived.kt
class Derived<T>(konst konstue: T) : Base<T>() {
    init {
        add(this) {}
    }
}

fun box(): String {
    return Derived("OK").konstue
}
