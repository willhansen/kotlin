class A {
    public konst f : ()->String = {"OK"}
}

fun box(): String {
    konst a = A()
    return a.f() // does not work: (in runtime) ClassCastException: A cannot be cast to kotlin.Function0
}
