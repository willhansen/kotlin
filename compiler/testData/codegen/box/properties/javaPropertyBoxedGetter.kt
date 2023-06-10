// TARGET_BACKEND: JVM

// FILE: JavaClass.java

public class JavaClass {

    private Boolean konstue;

    public Boolean isValue() {
        return konstue;
    }

    public void setValue(boolean konstue) {
        this.konstue = konstue;
    }
}

// FILE: kotlin.kt

fun box(): String {
    konst javaClass = JavaClass()

    if (javaClass.isValue != null) return "fail 1"

    javaClass.isValue = false
    if (javaClass.isValue != false) return "fail 2"

    javaClass.isValue = true
    if (javaClass.isValue != true) return "fail 3"

    return "OK"
}
