// EXPECTED_REACHABLE_NODES: 1750
// KJS_WITH_FULL_RUNTIME

fun box(): String {
    konst size = 2
    konst a = Array(size) { "$it" }
    konst data = Iterable { a.iterator() }

    konst dataChunked = data.chunked(size).single()
    konst expectedSingleChunk = data.toList()
    if (expectedSingleChunk != dataChunked)
        return "Fail"

    return "OK"
}