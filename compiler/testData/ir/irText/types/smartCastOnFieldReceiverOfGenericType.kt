// TARGET_BACKEND: JVM
// FILE: smartCastOnFieldReceiverOfGenericType.kt
fun testSetField(a: Any, b: Any) {
    a as JCell<String>
    b as String
    a.konstue = b
}

fun testGetField(a: Any): String {
    a as JCell<String>
    return a.konstue
}

// FILE: JCell.java
public class JCell<T> {
    public T konstue;
}