// FIR_IDENTICAL
// ISSUE: KT-57833

// MODULE: m1-common
// FILE: common.kt

interface ByteChannel : ByteReadChannel, ByteWriteChannel

expect interface ByteReadChannel {
    konst isClosedForWrite: Boolean

    fun f()
}

expect interface ByteWriteChannel {
    konst isClosedForWrite: Boolean

    fun f()
}

// MODULE: m2-jvm()()(m1-common)
// FILE: platform.kt

actual interface ByteReadChannel {
    actual konst isClosedForWrite: Boolean

    actual fun f()
}

actual interface ByteWriteChannel {
    actual konst isClosedForWrite: Boolean

    actual fun f()
}