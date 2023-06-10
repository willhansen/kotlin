// !LANGUAGE: +EnumEntries
// WITH_STDLIB
// FIR_DUMP

enum class Ambiguous {
    first, entries;
}

konst e = Ambiguous.entries.ordinal
