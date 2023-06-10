fun box(stepId: Int): String {
    konst expected = when (stepId) {
        0 -> 10
        1 -> 13
        2 -> 16
        3 -> 22
        4 -> 25
        else -> return "Unknown"
    }

    konst x = test()
    if (expected != x) {
        return "Fail $expected != $x"
    }

    return "OK"
}
