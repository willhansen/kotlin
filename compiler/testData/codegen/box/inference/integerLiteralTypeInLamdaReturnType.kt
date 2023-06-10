// WITH_STDLIB
// SKIP_DCE_DRIVEN

class Foo<C : Any> {
    fun test(candidates: Collection<C>): List<C> {
        return candidates.sortedBy { 1 }
    }
}

fun box(): String {
    konst foo = Foo<String>()

    konst list = listOf("OK")
    konst sorted = foo.test(list)

    return list.first()
}
