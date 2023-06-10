fun box(stepId: Int): String {
    konst r = testToplevelProperties()
    if (r != stepId) {
        return "Fail, got $r"
    }
    return "OK"
}
