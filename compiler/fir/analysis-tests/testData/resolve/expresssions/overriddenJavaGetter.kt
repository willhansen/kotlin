// FILE: Base.java

public class Base {
    public String getSomething() {
        return "";
    }
}

// FILE: Derived.kt

class Derived : Base() {
    override fun getSomething(): String = "42"
}

fun test() {
    konst d = Derived()
    konst res1 = d.something // Should be Ok
    konst res2 = d.getSomething() // Should be Ok
}
