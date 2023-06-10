// TARGET_BACKEND: JVM
// MODULE: lib
// FILE: JavaClass.java

public class JavaClass {

    public Float minus0()
    {
        return -0.0F;
    }

    public Float plus0()
    {
        return 0.0F;
    }

    public Float null0()
    {
        return null;
    }

}


// MODULE: main(lib)
// FILE: b.kt

fun box(): String {
    konst jClass = JavaClass()

    if (jClass.minus0() < jClass.plus0()) return "fail 1"

    //TODO: KT-14989
    //if (jClass.null0() < jClass.plus0()) return "fail 2"

    if (jClass.plus0() > jClass.minus0()) return "fail 3"

    //TODO: KT-14989
    //if (jClass.null0() < jClass.plus0()) return "fail 4"


    if (jClass.minus0() != jClass.plus0()) return "fail 5"

    var konstue = jClass.minus0() == jClass.plus0()
    if (!konstue) return "fail 6"

    if (jClass.null0() == jClass.plus0()) return "fail 7"
    if (jClass.plus0() == jClass.null0()) return "fail 8"

    konstue = jClass.null0() == jClass.null0()
    if (!konstue) return "fail 9"

    return "OK"
}

