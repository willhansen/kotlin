// !LANGUAGE: -ProhibitVarargAsArrayAfterSamArgument
// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// FIR status: don't support legacy feature
// TARGET_BACKEND: JVM

// FILE: Test.java
public class Test {
    public static String foo1(Runnable r, String... strs) {
        return null;
    }
    public String foo2(Runnable r1, Runnable r2, String... strs) {
        return null;
    }
    public Test(Runnable r, String... strs) {}
    public Test(Runnable r1, Runnable r2, String... strs) {}
}

// FILE: main.kt
fun box(): String {
    konst x1 = {}
    konst x2: Runnable = Runnable { }
    konst x3 = arrayOf<String>()

    Test.foo1({}, arrayOf())
    Test.foo1({}, *arrayOf())
    Test.foo1({}, x3)
    Test.foo1({}, *arrayOf(""))

    Test.foo1(x1, arrayOf())
    Test.foo1(x1, *arrayOf())
    Test.foo1(x2, *arrayOf())

    Test.foo1(x1, x3)
    Test.foo1(x1, *x3)
    Test.foo1(x2, *arrayOf(""))

    konst i1 = Test({}, arrayOf())
    konst i2 = Test({}, *arrayOf())
    konst i3 = Test({}, x3)
    konst i4 = Test({}, arrayOf(""))
    konst i5 = Test({}, {}, *arrayOf(""))
    konst i6 = Test({}, {}, arrayOf())

    i1.foo2({}, {}, arrayOf())
    i2.foo2({}, {}, *arrayOf())
    i3.foo2(x2, {}, *arrayOf())

    i4.foo2({}, {}, arrayOf(""))
    i5.foo2({}, {}, *x3)
    i6.foo2(x2, {}, *arrayOf(""))

    return "OK"
}
