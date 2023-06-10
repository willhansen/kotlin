// !LANGUAGE: -EnumEntries
// WITH_STDLIB

enum class E {
    ;

    konst entries: Int = 0
}

fun test() {
    E::<!DEPRECATED_ACCESS_TO_ENUM_ENTRY_PROPERTY_AS_REFERENCE!>entries<!>
    konst ref = E::<!DEPRECATED_ACCESS_TO_ENUM_ENTRY_PROPERTY_AS_REFERENCE!>entries<!>
    konst refType: (E) -> Int = E::entries
    konst refTypeWithAnyExpectedType: Any = E::<!DEPRECATED_ACCESS_TO_ENUM_ENTRY_PROPERTY_AS_REFERENCE!>entries<!>
}
