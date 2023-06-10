class A {
    konst a = ""
    fun b() = ""

    fun test() {
        konst a = A::a.name
        konst b = A::b.name
        konst c = ::A.name

        konst d = this::a.name
        konst e = A()::b.name
    }
}
