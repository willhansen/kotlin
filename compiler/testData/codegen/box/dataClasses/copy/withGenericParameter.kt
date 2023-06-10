data class A(konst a: Foo<String>) {}

class Foo<T>(konst a: T) { }

fun box() : String {
    konst f1 = Foo("a")
    konst f2 = Foo("b")
    konst a = A(f1)
    konst b = a.copy(f2)
    if (b.a.a == "b") {
        return "OK"
    }
    return "fail"
}
