// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

class JavaClass {
    void set(Runnable i, Runnable konstue) {
        i.run();
        konstue.run();
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst obj = JavaClass()

    var v = "FAIL"
    obj[{ v = "O" }] = { v += "K" }
    return v
}
