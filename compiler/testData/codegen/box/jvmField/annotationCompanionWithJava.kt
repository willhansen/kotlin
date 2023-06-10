// !LANGUAGE: +JvmFieldInInterface +NestedClassesInAnnotations
// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Test.java

public class Test {
    public static String publicField() {
        return Foo.z.getS();
    }
}

// FILE: simple.kt


public class Bar(public konst s: String)

annotation class Foo {

    companion object {
        @JvmField
        konst z = Bar("OK")
    }
}


fun box(): String {
    return Test.publicField()
}
