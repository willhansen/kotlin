// FILE: call.kt
fun call() {
    konst javaClass = JavaClass()
    javaClass.<expr>foo</expr>
}

// FILE: JavaClass.java
class JavaClass {
    int getFoo() { return 42; }
}
