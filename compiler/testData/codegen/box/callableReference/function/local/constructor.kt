fun box(): String {
    class A {
        konst result = "OK"
    }

    return (::A).let { it() }.result
}
