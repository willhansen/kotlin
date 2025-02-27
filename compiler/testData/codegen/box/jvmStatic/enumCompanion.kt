// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Test.java

class Test {
    public static String foo() {
        return A.foo;
    }

    public static String constBar() {
        return A.constBar;
    }

    public static String getBar() {
        return A.getBar();
    }

    public static String baz() {
        return A.baz();
    }
}

// FILE: enumCompanionObject.kt

enum class A {
    ;
    companion object {
        @JvmField konst foo: String = "OK"

        const konst constBar: String = "OK"

        @JvmStatic konst bar: String = "OK"

        @JvmStatic fun baz() = foo
    }
}

fun box(): String {
    if (Test.foo() != "OK") return "Fail foo"
    if (Test.constBar() != "OK") return "Fail bar"
    if (Test.getBar() != "OK") return "Fail getBar"
    if (Test.baz() != "OK") return "Fail baz"
    return "OK"
}
