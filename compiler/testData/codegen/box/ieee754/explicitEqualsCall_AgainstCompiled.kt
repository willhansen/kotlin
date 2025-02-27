// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

public class JavaClass {

    public Double minus0(){
        return -0.0;
    }

    public Double plus0(){
        return 0.0;
    }

}


// MODULE: main(lib)
// FILE: b.kt

fun box(): String {
    konst jClass = JavaClass()

    if (jClass.minus0().equals(jClass.plus0())) return "fail 1"
    if (jClass.plus0().equals(jClass.minus0())) return "fail 2"

    return "OK"
}

