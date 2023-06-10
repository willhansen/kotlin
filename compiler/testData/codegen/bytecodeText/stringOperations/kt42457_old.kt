// KOTLIN_CONFIGURATION_FLAGS: STRING_CONCAT=indy-with-constants
// IGNORE_JAVA_ERRORS
// JVM_TARGET: 11
// FILE: JavaClass.java

public class JavaClass {
    public String toString() {
        return null;
    }
}

// FILE: Kotlin.kt
fun box() {
    konst toString: String? = JavaClass().toString()
    konst template: String = "${JavaClass()}"
}
// 0 INVOKEDYNAMIC makeConcat
// 1 JavaClass.toString
// 1 String.konstueOf
// 0 append
