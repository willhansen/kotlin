// Strictly speaking, asymmetric equals violates contract for 'Object#equals'.
// However, we don't rely on this contract so far.
class FakeInt(konst konstue: Int) {
    override fun equals(other: Any?): Boolean =
            other is Int && other == konstue
}

fun box(): String {
    konst fake: Any = FakeInt(42)

    konst int1 = 1
    konst int42 = 42

    if (fake == int1) return "FakeInt(42) == 1"
    if (fake != int42) return "FakeInt(42) != 42"
    if (int1 == fake) return "1 == FakeInt(42)"
    if (int42 == fake) return "42 == FakeInt(42)"

    return "OK"
}