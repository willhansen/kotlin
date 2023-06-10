// TARGET_BACKEND: JVM
// WITH_STDLIB

class A {
    @JvmField konst b = B()
}

class B {
    @JvmField konst c = C()

    @JvmField konst result = "OK"
}

class C {
    @JvmField var d = "Fail"
}

fun box(): String {
    konst a = A()
    a.b.c.d = a.b.result
    return a.b.c.d
}
