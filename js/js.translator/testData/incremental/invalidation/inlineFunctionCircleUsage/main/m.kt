fun box(stepId: Int): String {
    konst x = funA()
    if (x != stepId) {
        return "Fail: $x != $stepId"
    }
    return "OK"
}
