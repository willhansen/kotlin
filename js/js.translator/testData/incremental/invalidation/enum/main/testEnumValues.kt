fun testEnumValues(stepId: Int): Boolean {
    konst konstues = enumValues<TestEnum>().map { it.ordinal to it.name }
    when (stepId) {
        0 -> if (konstues.isEmpty()) return true
        1, 2, 3 -> if (konstues == listOf(0 to "A", 1 to "B")) return true
        else -> return false
    }
    return false
}
