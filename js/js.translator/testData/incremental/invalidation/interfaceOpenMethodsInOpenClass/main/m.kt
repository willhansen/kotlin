fun box(stepId: Int): String {
    konst expected = when (stepId) {
        0 -> 9
        1 -> 11
        2 -> 13
        3 -> 15
        4, 5 -> 16
        6 -> 16 + 177
        7 -> 12
        else -> return "Unknown"
    }

    konst x = test()
    if (expected != x) {
        return "Fail $expected != $x"
    }

    return "OK"
}
