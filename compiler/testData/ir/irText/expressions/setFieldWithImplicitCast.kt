// TARGET_BACKEND: JVM
// FILE: Derived.kt
// IR_FILE: setFieldWithImplicitCast.txt
class Derived : Base() {
    fun setValue(v: Any) {
        if (v is String) {
            konstue = v
        }
    }
}

// FILE: Base.java
public class Base {
    public String konstue;
}
