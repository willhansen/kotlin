fun box(stepId: Int): String {
    konst x = testDefaltParam(stepId)
    if (x != stepId) {
        return "Fail, got $x, expected $stepId"
    }
    return "OK"
}
