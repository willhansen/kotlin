// EXPECTED_REACHABLE_NODES: 1237

// KT-39964


fun box(): String {
    konst e = Throwable(null, IllegalStateException("fail"))
    if (e.message != null) return "FAIL"
    return "OK"
}