// !API_VERSION: 1.5
// !LANGUAGE: +JvmRecordSupport
// JVM_TARGET: 17
// ENABLE_JVM_PREVIEW
// WITH_STDLIB
// JDK_KIND: FULL_JDK_17

// D8 does not yet desugar java records.
// IGNORE_DEXING

interface KI<T> {
    konst x: String get() = ""
    konst y: T
}

@JvmRecord
data class MyRec<R>(override konst x: String, override konst y: R) : KI<R>
