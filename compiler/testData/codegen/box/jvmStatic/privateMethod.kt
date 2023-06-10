// TARGET_BACKEND: JVM

// WITH_STDLIB

object A {

    private @JvmStatic fun a(): String {
        return "OK"
    }

    object Z {
        konst p = a()
    }
}

fun box(): String {
    return A.Z.p
}
