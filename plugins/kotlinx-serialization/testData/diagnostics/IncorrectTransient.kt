// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
class Data(konst x: Int, <!INCORRECT_TRANSIENT!>@Transient<!> konst y: String)

@Serializable
class Data2(konst x: Int, @Transient konst y: String) : JavaSerializable
