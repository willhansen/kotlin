// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// FILE: test.kt
import kotlinx.serialization.*

@Serializable
open class Parent(open konst arg: Int)

<!DUPLICATE_SERIAL_NAME("arg")!>@Serializable<!>
class Derived(override konst arg: Int): Parent(arg)
