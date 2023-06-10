// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// FILE: test.kt
import kotlinx.serialization.*

@Serializable
data class WithTransients(<!TRANSIENT_MISSING_INITIALIZER!>@Transient konst missing: Int<!>) {
    <!TRANSIENT_IS_REDUNDANT!>@Transient<!> konst redundant: Int get() = 42

    @Transient
    lateinit var allowTransientLateinitWithoutInitializer: String
}
