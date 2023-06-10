// TARGET_BACKEND: JVM
// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// FIR status: scripts aren't supported yet
// IGNORE_LIGHT_ANALYSIS
// WITH_STDLIB
// FILE: test.kt

fun box(): String =
    Script.Build.Debug.run { "${c0()}${c1()}" }

// FILE: script.kts

interface Base {
    konst v: String

    fun c0(): Char {
        fun getC0() = v[0]
        return getC0()
    }
}

enum class Build(override konst v: String): Base {
    Debug("OK"),
    Release("NO");

    fun c1(): Char {
        konst g = object {
            konst c1 = v[1]
        }
        return g.c1
    }
}