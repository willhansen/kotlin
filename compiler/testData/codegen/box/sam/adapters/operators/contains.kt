// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

class JavaClass {
    boolean contains(Runnable i) {
        i.run();
        return true;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst obj = JavaClass()

    var v = "FAIL"
    { v = "O" } in obj
    { v += "K" } !in obj
    return v
}
