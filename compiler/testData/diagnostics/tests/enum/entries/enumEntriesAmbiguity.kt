// !LANGUAGE: +EnumEntries
// WITH_STDLIB
// FIR_DUMP

enum class Ambiguous {
    first, <!DEPRECATED_DECLARATION_OF_ENUM_ENTRY!>entries;<!>
}

konst e = Ambiguous.entries.ordinal
