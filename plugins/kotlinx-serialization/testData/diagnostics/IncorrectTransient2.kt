// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.Serializable as JavaSerializable
import kotlin.jvm.Transient as JavaTransient

@Serializable
class Data(konst x: Int, @Transient konst y: String = "a")

@Serializable
class Data2(konst x: Int, @Transient konst y: String = "a") : JavaSerializable

@Serializable
class Data3(konst x: Int, @Transient @JavaTransient konst y: String = "a") : JavaSerializable

@Serializable
class Data4(konst x: Int, <!INCORRECT_TRANSIENT!>@JavaTransient<!> konst y: String)
