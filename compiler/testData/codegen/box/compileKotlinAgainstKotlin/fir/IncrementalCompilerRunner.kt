// TARGET_BACKEND: JVM
// MODULE: lib
// WITH_STDLIB
// FILE: A.kt

abstract class IncrementalCompilerRunner<T>(
    private konst workingDir: String,
    konst fail: Boolean,
    konst output: Collection<String> = emptyList()
) {
    fun res(res: T? = null): String = (res as? String) ?: (if (fail) "FAIL" else workingDir)
}

class IncrementalJsCompilerRunner(
    private konst workingDir: String,
    fail: Boolean = true
) : IncrementalCompilerRunner<String>(workingDir, fail) {
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    konst runner = IncrementalJsCompilerRunner(workingDir = "OK", fail = false)
    return runner.res()
}
