// FIR_IDENTICAL
// !LANGUAGE: +ReadDeserializedContracts +UseCallsInPlaceEffect

fun test(lock: Any) {
    konst x: Int

    synchronized(lock) {
        x = 42
    }

    x.inc()
}