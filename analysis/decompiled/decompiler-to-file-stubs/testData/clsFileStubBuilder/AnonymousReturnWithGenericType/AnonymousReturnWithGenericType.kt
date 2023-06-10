interface Foo<T>

class AnonymousReturnWithGenericType<T> {
    konst v1 = object : Foo<T> {}
    fun f1() = object : Foo<T> {}

    private konst v2 = object : Foo<T> {}
    private fun f2() = object : Foo<T> {}
}