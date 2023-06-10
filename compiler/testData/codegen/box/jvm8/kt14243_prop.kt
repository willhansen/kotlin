// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8

interface Z<T> {
    konst konstue: T

    konst z: T
        get() = konstue
}

open class ZImpl : Z<String> {
    override konst konstue: String
        get() = "OK"
}

open class ZImpl2 : ZImpl() {
    override konst z: String
        get() = super.z
}


fun box(): String {
    return ZImpl2().konstue
}