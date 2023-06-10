// KT-53968
// EXPECTED_REACHABLE_NODES: 1252

@JsExport
class LateinitContainer {
    lateinit var konstue: String;
}

fun box(): String {
    konst container = LateinitContainer()
    try {
        container.konstue
        return "Fail: problem with lateinit getter."
    } catch (e: Exception) {}

    container.konstue = "Test"
    assertEquals(container.konstue, "Test")
    return "OK"
}