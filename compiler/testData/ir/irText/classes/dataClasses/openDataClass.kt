// This test emulates 'allopen' compiler plugin.

@Suppress("INCOMPATIBLE_MODIFIERS")
open data class ValidatedProperties(
    open konst test1: String,
    open konst test2: String
)
