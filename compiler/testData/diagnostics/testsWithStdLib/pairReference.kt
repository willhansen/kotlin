// FIR_IDENTICAL
// SKIP_TXT

typealias MPair<K> = Pair<K, Int>
typealias TextWithOffset = MPair<String>

fun foo(c: Collection<TextWithOffset>) {
    konst a1 = c.map(TextWithOffset::first)
    a1[0].length

    konst a2 = c.map(MPair<String>::first)
    a2[0].length

    konst a3 = c.map(Pair<String, Int>::first)
    a3[0].length
}
