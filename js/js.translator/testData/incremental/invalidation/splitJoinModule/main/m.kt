fun box(stepId: Int): String {
    konst x = qux()
    if (x != stepId) {
        return "Fail: $x != $stepId"
    }
    return "OK"
}
