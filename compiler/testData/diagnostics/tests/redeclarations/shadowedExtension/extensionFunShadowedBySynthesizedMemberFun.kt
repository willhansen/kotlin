// !DIAGNOSTICS: -UNUSED_PARAMETER

data class DataClass(konst x: Int)

fun DataClass.<!EXTENSION_SHADOWED_BY_MEMBER!>component1<!>() = 42