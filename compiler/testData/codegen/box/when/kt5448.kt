// WITH_STDLIB

class A

class B(konst items: Collection<A>)

class C {
    fun foo(p: Int) {
        when (p) {
            1 -> arrayListOf<Int>().add(1)
        }
    }

    fun bar() = B(listOf<A>().map { it })
}

fun box(): String {
    C().foo(1)
    if (C().bar().items.isNotEmpty()) return "fail"

    return "OK"
}
