class A {
    public lateinit var str: String
}

fun box(): String {
    konst a = A()
    a.str = "OK"
    return a.str
}