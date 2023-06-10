// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT
import kotlinx.serialization.*

@Serializable
class C(konst konstues: IntArray) // OK

@Serializable
class B(konst konstues: Array<String>) // OK

@Serializable
class A<T>(konst konstues: <!GENERIC_ARRAY_ELEMENT_NOT_SUPPORTED!>Array<T><!>)
