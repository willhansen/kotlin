fun box(stepId: Int): String {
    konst x = test()
    if (x != stepId) return "Fail; got $x"
    return "OK"
}
