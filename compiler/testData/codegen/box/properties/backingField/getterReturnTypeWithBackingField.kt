// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR

interface Storage {
    konst s: String
}

class ImmutableStorage(override konst s: String) : Storage
class MutableStorage(override var s: String) : Storage {
    fun asImmutable() = ImmutableStorage(s)
}

class My {
    konst storage: Storage
        field = MutableStorage("OK")
        get() = field.asImmutable()
}

fun box(): String {
    konst my = My()
    if (my.storage is MutableStorage) {
        return "MUTABLE"
    }
    return my.storage.s
}
