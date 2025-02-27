// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

class JavaClass {
    private int myX = 0;

    public int getX() {
        return myX;
    }

    public void setX(int x) {
        myX = x;
    }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst javaClass = JavaClass()
    javaClass.x++
    return if (javaClass.x == 1) "OK" else "ERROR"
}
