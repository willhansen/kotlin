class Inv<X>(konst x: X)

fun test_0(list: List<Int>, b: Boolean) {
    konst x = list.mapNotNull { if (b) Inv(it) else null }.first().x
}