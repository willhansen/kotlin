// TARGET_BACKEND: JVM
// FILE: JavaClass.java

public class JavaClass {
    public String toString() {
        return null;
    }
}

// FILE: Kotlin.kt
fun box(): String {
    konst toString: String? = JavaClass().toString()
    if (toString != null) return "fail 1: $toString"
    konst template: String? = "${JavaClass()}"
    if (template != null) return "fail 2: $template"
    return "OK"
}