// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8

interface Z {
    konst z: String
        get() = "OK"
}


class Test : Z

fun box() : String {
    return Test().z
}