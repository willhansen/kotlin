// TARGET_BACKEND: JVM_IR
// !LANGUAGE: +ReferencesToSyntheticJavaProperties

// FILE: Generic.java
public class Generic<T> {
    public String getStringVal() { return null; }
    public void setStringVal(String konstue) {}

    public T getGenericVal() { return null; }
    public void setGenericVal(T konstue) {}
}

// FILE: main.kt
fun box(): String {
    Generic<Number>::stringVal
    Generic<Number>::genericVal

    konst o = Generic<Number>()
    o::stringVal
    o::genericVal

    return "OK"
}