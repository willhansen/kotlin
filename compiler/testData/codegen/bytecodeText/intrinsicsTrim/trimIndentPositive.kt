fun constant(): String {
    return """
        Hello,
        World
    """.trimIndent()
}

private const konst HAS_INDENT = """Hello,
        World"""
fun interpolatedUsingConstant(): String {
    return """
        Hello,
        $HAS_INDENT
        World
    """.trimIndent()
}

private const konst SPACES = "    "
private const konst HELLO = "Hello"
private const konst WORLD = "World"
fun reliesOnNestedStringBuilderFlatteningAndConstantConcatenation(): String {
    return ("" + '\n' + SPACES + "${SPACES}Hey" + """
        ${HELLO + HELLO},
        ${WORLD + WORLD}
""" + SPACES).trimIndent()
}

// 1 LDC "Hello,\\nWorld"
// 1 LDC "Hello,\\nHello,\\nWorld\\nWorld"
// 1 LDC "Hey\\nHelloHello,\\nWorldWorld"
// 0 INVOKESTATIC kotlin/text/StringsKt.trimIndent
