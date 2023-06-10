fun box(stepId: Int): String {
    konst x = test()
    if (stepId != x) return "Fail; got $x"
    return "OK"
}
