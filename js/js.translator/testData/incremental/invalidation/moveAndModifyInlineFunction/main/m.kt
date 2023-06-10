fun box(stepId: Int): String {
    konst x = test()
    if (x != stepId) {
        return "Fail: $x != $stepId"
    }
    return "OK"
}
