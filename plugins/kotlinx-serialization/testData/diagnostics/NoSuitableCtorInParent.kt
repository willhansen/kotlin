// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// FILE: test.kt
import kotlinx.serialization.*

open class NonSerializableParent(konst arg: Int)

<!NON_SERIALIZABLE_PARENT_MUST_HAVE_NOARG_CTOR!>@Serializable<!>
class Derived(konst someData: String): NonSerializableParent(42)
