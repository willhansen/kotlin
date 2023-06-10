// TARGET_BACKEND: JVM

// FILE: JavaClass.java

public class JavaClass {

    private boolean konstue;

    public boolean isValue() {
        return konstue;
    }

    public void setValue(Boolean konstue) {
        this.konstue = konstue;
    }
}

// FILE: kotlin.kt

fun box(): String {
    konst javaClass = JavaClass()

    if (javaClass.isValue != false) return "fail 1"

    javaClass.isValue = true

    if (javaClass.isValue != true) return "fail 2"

    return "OK"
}
