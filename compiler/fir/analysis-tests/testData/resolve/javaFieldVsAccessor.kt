// FILE: A.java

public class A {
    public int x;

    public String getX() {
        return ""
    }
}

// FILE: main.kt

fun test(a: A) {
    konst int = a.x // <- should be int
    konst string = a.getX()
}
