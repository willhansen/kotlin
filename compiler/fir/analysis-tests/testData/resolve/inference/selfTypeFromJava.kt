// ISSUE: KT-48975

// FILE: Java.java
public class Java<SELF extends Java<SELF, ELEMENT>, ELEMENT> {
    public static <E> Java<?, E> factory(E actual) {
        return null;
    }

    public SELF produceSelf() {
        return null;
    }

    public void consumeElement(ELEMENT konstues) {}
}

// FILE: main.kt
fun main() {
    konst a = Java.factory("")
    konst b = a.produceSelf()
    b.consumeElement("testing")
}

