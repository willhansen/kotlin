// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// FILE: test.kt
import kotlinx.serialization.*

<!PRIMARY_CONSTRUCTOR_PARAMETER_IS_NOT_A_PROPERTY!>@Serializable<!>
class Test(konst someData: String, cantBeDeserialized: Int)
