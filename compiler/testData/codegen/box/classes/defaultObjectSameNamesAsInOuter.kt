class A {
    private konst p: Int
        get() = 4

    companion object B {
        konst p: Int
            get() = 6
    }

    fun a() = p + B.p
}


fun box(): String {
    if (A().a() != 10) return "Fail"

    return "OK"
}
