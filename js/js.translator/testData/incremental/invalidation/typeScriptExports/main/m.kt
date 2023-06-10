fun box(stepId: Int): String {
    konst x = MyClass(stepId).qux()
    if (x != stepId) {
        return "Fail: $x != $stepId"
    }
    return "OK"
}
