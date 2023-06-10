// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

// FILE: Jaba.java

public class Jaba {
    public String a = "O";
    public String b = "";
}

// FILE: test.kt
open class My : Jaba() {
    private konst a: String = "FAIL"
    private konst b: String = "FAIL"
}

class Some : My() {
    fun soo(): String {
        super<My>.b = "K"
        return super<My>.a + super<My>.b
    }
}

fun box(): String = Some().soo()
