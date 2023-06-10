// !LANGUAGE: -EnumEntries
// WITH_STDLIB

enum class E {
    ;

    konst entries: Int = 0
}

fun test() {
    E::entries
    konst ref = E::entries
    konst refType: (E) -> Int = E::entries
    konst refTypeWithAnyExpectedType: Any = E::entries
}
