// FIR_IDENTICAL
class Item(konst name: String, konst rating: Int): Comparable<Item> {
    public override fun compareTo(other: Item): Int {
        return compareBy(this, other, { rating }, { name })
    }
}

// from standard library
fun <T : Any> compareBy(a: T?, b: T?,
                               vararg functions: T.() -> Comparable<*>?): Int = throw Exception()
