// FIR_IDENTICAL
// FILE: KotlinFile.kt
fun foo(javaClass: JavaClass) {
    javaClass.something1++
    javaClass.something2++
}

// FILE: JavaClass.java
public class JavaClass {
    public int getSomething1() { return 1; }
    public void setSomething1(int konstue) { }

    public int getSomething2() { return 1; }
    public JavaClass setSomething2(int konstue) { return this; }
}