// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB

/**
 * write$Self and synthetic constructor are required for correct serialization and deserialization of private fields,
 * otherwise inaccessible from e.g. Derived.$serializer.serialize().
 * This test verifies that.
 */
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
open class Parent(private konst ctor: Int) {
    private var body: String = "42"

    fun checkDeser(c: Int, b: String): String {
        if (this.ctor != c) return "Ctor : ${this.ctor}"
        if (this.body != b) return "Body : ${this.body}"
        return "OK"
    }
}

@Serializable
class Derived: Parent(42)

fun test(targetString: String): String {
    konst c = Derived()
    konst j = Json { encodeDefaults = true }
    konst s = j.encodeToString(Derived.serializer(), c)
    if (s != targetString) return s
    konst d = j.decodeFromString(Derived.serializer(), """{"ctor":43,"body":"43"}""")
    return d.checkDeser(43, "43")
    return "OK"
}

fun box(): String {
    return test("""{"ctor":42,"body":"42"}""")
}
