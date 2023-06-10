// TARGET_BACKEND: JVM
// WITH_STDLIB
// MODULE: lib
// FILE: Custom.java

class Custom {
    public interface Runnable {
        void run2();
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst f = { }
    konst class1 = Runnable(f).javaClass
    konst class2 = Custom.Runnable(f).javaClass

    return if (class1 != class2) "OK" else "Same class: $class1"
}
