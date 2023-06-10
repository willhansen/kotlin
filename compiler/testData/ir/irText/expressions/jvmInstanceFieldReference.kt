// TARGET_BACKEND: JVM
// FILE: Derived.kt
// IR_FILE: jvmInstanceFieldReference.txt
class Derived: Base() {
    init {
        konstue = 0
    }

    fun getValue() = konstue

    fun setValue(konstue: Int) {
        this.konstue = konstue
    }
}

// FILE: Base.java
public class Base {
    public int konstue;
}

