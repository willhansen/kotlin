// TARGET_BACKEND: JVM

// FILE: J.java

public class J {

    public static int test1() {
        A<String, B<String>> x = new X<String, B<String>>("O", new B<String>("K"));
        return A.DefaultImpls.test1(x, 1, 1.0);
    }


    public static A<String, B<String>> test2(){
        X<String, B<String>> x = new X<String, B<String>>("O", new B<String>("K"));
        return A.DefaultImpls.test2(x, 1);
    }
}

// FILE: K.kt

class B<T>(konst konstue: T)

interface A<T, Y : B<T>> {

    fun <T, L> test1(p: T, z: L): T {
        return p
    }

    fun <L> test2(p: L): A<T, Y> {
        return this
    }
}


class X<T, Y : B<T>>(konst p1: T, konst p2: Y) : A<T, Y> {

}

fun box(): String {
    konst test1 = J.test1()
    if (test1 != 1) return "fail 1: $test1 != 1"

    konst test2: X<String, B<String>> = J.test2() as X<String, B<String>>

    return test2.p1 + test2.p2.konstue
}
