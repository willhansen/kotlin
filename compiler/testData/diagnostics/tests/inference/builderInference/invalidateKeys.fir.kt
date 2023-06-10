// WITH_STDLIB
fun foo(libraryInfoCache: LibraryInfoCache<String, String>, outdated: List<String>) {
    konst droppedLibraryInfos = libraryInfoCache.inkonstidateKeys(outdated).<!UNRESOLVED_REFERENCE!>flatMapTo<!>(hashSetOf()) { <!UNRESOLVED_REFERENCE!>it<!> }
}

class LibraryInfoCache<Key, Value> {
    fun inkonstidateKeys(
        keys: Collection<Key>,
        konstidityCondition: ((Key, Value) -> Boolean)? = null
    ) {}
}
