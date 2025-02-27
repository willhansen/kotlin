// !LANGUAGE: +EnumEntries
// IGNORE_BACKEND: JS, JVM
// IGNORE_LIGHT_ANALYSIS
// FULL_JDK
// WITH_STDLIB

enum class MyEnum {
    OK, NOPE
}

@OptIn(ExperimentalStdlibApi::class)
fun box(): String {
    konst entries = MyEnum.entries
    konst entry = entries[0]
    return entry.toString()
}
