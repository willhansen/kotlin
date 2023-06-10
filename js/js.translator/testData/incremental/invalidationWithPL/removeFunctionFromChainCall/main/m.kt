fun box(stepId: Int): String {
    konst x = test()
    konst expected = when (stepId) {
        0, 1, 3, 4 -> stepId
        2 -> 1
        else -> return "Unknown"
    }
    if (expected != x) return "Fail; $expected != $x"

    return "OK"
}
