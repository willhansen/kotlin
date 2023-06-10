// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass2.java

class JavaClass1 {
    protected Object konstue = null;

    public Object getSomething() { return null; }
    public void setSomething(Object konstue) {  this.konstue = konstue; }
}

class JavaClass2 extends JavaClass1 {
    public String getSomething() { return (String)konstue; }
}

// MODULE: main(lib)
// FILE: 1.kt

fun box(): String {
    konst javaClass = JavaClass2()
    javaClass.something = "OK"
    return javaClass.something
}
