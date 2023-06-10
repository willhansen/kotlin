class Interner<T> {
    private fun find(obj: T): Int? = null

    fun intern(obj: T) = find(obj)
}

class Externer {
    private fun find(obj: Int): Int? = null

    fun intern(obj: Int) = find(obj)
}

interface Some {
    data class WithPrimary(konst x: Int, konst arr: Array<String>? = null, konst s: String? = null)
}

fun test() {
    Some.WithPrimary(42, arrayOf("alpha", "omega"))
}

class KonanTarget(konst name: String)

konst KonanTarget.presetName
    get() = this.name

// Own private property conflicts with synthetic property from Java supertype
// Exhaustive when expressions give Any result type
// Substitution for field declared in Java super-type does not work (KotlinStringLiteralTextEscaper.myHost)
// Super is not resolved in anonymous object
// TypeParameterDescriptor.name is not resolved
