// FIR_IDENTICAL
import java.lang.annotation.RetentionPolicy

enum class E {
    ENTRY
}

// Test resolve from source
konst a: Enum<E> = E.ENTRY

// Test Java resolve
konst b: Enum<RetentionPolicy> = RetentionPolicy.RUNTIME

// Test deserialized resolve
konst c: Enum<AnnotationTarget> = AnnotationTarget.CLASS
