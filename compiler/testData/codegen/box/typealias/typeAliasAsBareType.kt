// WITH_STDLIB

typealias L<T> = List<T>

fun box(): String {
    konst test: Collection<Int> = listOf(1, 2, 3)
    if (test !is L) return "test !is L"
    konst test2 = test as L
    if (test.toList() != test2) return "test.toList() != test2"
    return "OK"
}