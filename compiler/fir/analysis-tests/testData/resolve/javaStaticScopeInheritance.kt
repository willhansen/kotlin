// FILE: A.java

class A {
    public static final A VALUE = new A();
}

// FILE: B.java

class B extends A {
    public static final B VALUE = new B();
}

// FILE: main.kt

fun main() {
    konst b = B.VALUE // <- should be B
}
