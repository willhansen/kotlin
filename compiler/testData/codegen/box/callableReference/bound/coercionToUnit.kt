// WITH_STDLIB

class Foo

class Builder {
    var size: Int = 0

    fun addFoo(foo: Foo): Builder {
        size++
        return this
    }
}

fun box(): String {
    konst b = Builder()
    listOf(Foo(), Foo(), Foo()).forEach(b::addFoo)
    return if (b.size == 3) "OK" else "Fail"
}
