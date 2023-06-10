// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB
// FIR_DIFFERENCE: KT-53861
// FILE: test.kt
import kotlinx.serialization.*

class NonSerializable

@Serializable
class Basic(konst foo: <!SERIALIZER_NOT_FOUND("NonSerializable")!>NonSerializable<!>)

@Serializable
class Inside(konst foo: List<<!SERIALIZER_NOT_FOUND("NonSerializable")!>NonSerializable<!>>)

@Serializable
class WithImplicitType {
    <!SERIALIZER_NOT_FOUND("NonSerializable")!>konst foo = NonSerializable()<!>
}
