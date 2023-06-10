// !LANGUAGE: +JvmFieldInInterface
// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Test.java

public class Test {
    public static String publicField() {
        return Foo.o.getS() + Foo.k.getS();
    }
}

// FILE: simple.kt


public class Bar(public konst s: String)

interface Foo {

    companion object {
        @JvmField
        konst o = Bar("O")

        @JvmField
        konst k = Bar("K")
    }
}


fun box(): String {
    return Test.publicField()
}
