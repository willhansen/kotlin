// !LANGUAGE: +EnumEntries
// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// IGNORE_BACKEND: JS, JVM
// WITH_STDLIB

enum class EnumWithClash {
    konstues,
    entries,
    konstueOf;
}

@OptIn(ExperimentalStdlibApi::class)
fun box(): String {
    konst ref = EnumWithClash::entries
    if (ref().toString() != "[konstues, entries, konstueOf]") return "FAIL 1"
    if (EnumWithClash.entries.toString() != "entries") return "FAIL 2"
    return "OK"
}