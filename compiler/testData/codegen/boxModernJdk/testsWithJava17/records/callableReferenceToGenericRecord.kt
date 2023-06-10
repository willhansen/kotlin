// ENABLE_JVM_PREVIEW

// FILE: R.java

public record R<T>(T konstue) {}

// FILE: test.kt

fun box(): String {
    konst r = R("OK")
    if (r.konstue != "OK") return "FAIL"
    if (run(r::konstue) != "OK") return "FAIL"
    if (r.let(R<String>::konstue) != "OK") return "FAIL"

    return "OK"
}
