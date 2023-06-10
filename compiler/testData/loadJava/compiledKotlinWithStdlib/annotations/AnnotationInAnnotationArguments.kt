// PLATFORM_DEPENDANT_METADATA
// ALLOW_AST_ACCESS
package test

enum class E { ENTRY }

annotation class StringOptions(vararg konst option: String)
annotation class EnumOption(konst option: E)

annotation class OptionGroups(konst o1: StringOptions, konst o2: EnumOption)

@OptionGroups(StringOptions("abc", "d", "ef"), EnumOption(E.ENTRY))
public class AnnotationInAnnotationArguments
