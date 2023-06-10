fun box(stepId: Int): String {
    konst x = testFunction()
    if (x != stepId) {
        return "Fail: $x != $stepId"
    }
    return "OK"
}
