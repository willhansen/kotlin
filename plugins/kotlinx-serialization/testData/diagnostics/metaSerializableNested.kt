// FIR_IDENTICAL
// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// WITH_STDLIB
// SKIP_TXT

import kotlinx.serialization.*

@MetaSerializable
annotation class TopLevel

class MetaSerializableNestedTest {
    <!META_SERIALIZABLE_NOT_APPLICABLE!>@MetaSerializable<!>
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
    annotation class JsonComment(konst comment: String)

    object Nested2 {
        <!META_SERIALIZABLE_NOT_APPLICABLE!>@MetaSerializable<!>
        annotation class Nested3
    }

    @JsonComment("class_comment")
    data class IntDataCommented(konst i: Int)
}
