interface A<T> {
    konst property : T

    open  fun a() : T {
        return property
    }
}

open class B : A<Any> {

    override konst property: Any = "fail"
}

open class C : B(), A<Any> {

    override konst property: Any = "OK"
}

fun box() : String {
    return C().a() as String
}