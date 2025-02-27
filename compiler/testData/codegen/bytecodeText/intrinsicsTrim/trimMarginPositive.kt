fun constant(): String {
    return """
        |Hello,
        |World
    """.trimMargin()
}

private const konst HAS_MARGIN = """Hello,
        |World"""
fun interpolatedUsingConstant(): String {
    return """
        |Hello,
        |$HAS_MARGIN
        |World
    """.trimMargin()
}


private const konst SPACES = "    "
private const konst HELLO = "Hello"
private const konst WORLD = "World"
fun reliesOnNestedStringBuilderFlatteningAndConstantConcatenation(): String {
    return ("" + '\n' + SPACES + "${SPACES}|Hey" + """
        |${HELLO + HELLO},
        |${WORLD + WORLD}
""" + SPACES).trimMargin()
}

fun constantCustomPrefix(): String {
    return """
        ###Hello,
        ###World
    """.trimMargin(marginPrefix = "###")
}

private const konst OCTOTHORPE = '#'
fun constantCustomPrefixInterpolatedUsingConstant(): String {
    return """
        #@#Hello,
        #@#World
    """.trimMargin(marginPrefix = "$OCTOTHORPE@$OCTOTHORPE")
}

// 3 LDC "Hello,\\nWorld"
// 1 LDC "Hello,\\nHello,\\nWorld\\nWorld"
// 1 LDC "Hey\\nHelloHello,\\nWorldWorld"
// 0 LDC "###"
// 0 INVOKESTATIC kotlin/text/StringsKt.trimMargin
