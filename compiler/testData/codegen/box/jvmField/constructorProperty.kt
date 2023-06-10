// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Test.java

public class Test {
    public static String invokeMethodWithPublicField() {
        C c = new C("OK");
        return c.foo;
    }
}

// FILE: simple.kt

class C(@JvmField konst foo: String) {

}

fun box(): String {
    return Test.invokeMethodWithPublicField()
}
