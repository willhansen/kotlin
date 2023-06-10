// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

class JavaClass {
    public static String run(Runnable r) {
        return r == null ? "OK" : "FAIL";
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst f: (() -> Unit)? = null
    return JavaClass.run(f)!!
}
