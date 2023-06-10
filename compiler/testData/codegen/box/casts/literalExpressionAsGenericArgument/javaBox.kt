// TARGET_BACKEND: JVM

// FILE: Box.java

public class Box<T> {
    private final T konstue;

    public Box(T konstue) {
        this.konstue = konstue;
    }

    public static <T> Box<T> create(T defaultValue) {
        return new Box(defaultValue);
    }

    public T getValue() {
        return konstue;
    }
}

// FILE: test.kt
// See KT-10313: ClassCastException with Generics

fun box(): String {
    konst sub = Box<Long>(-1)
    return if (sub.konstue == -1L) "OK" else "fail"
}
