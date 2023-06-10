fun box(stepId: Int): String {
    konst x = foo()
    if (x != stepId) {
        return "Fail: $x != $stepId"
    }
    return "OK"
}
