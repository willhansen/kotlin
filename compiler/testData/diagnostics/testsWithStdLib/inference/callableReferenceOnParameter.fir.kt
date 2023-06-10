// Issue: KT-37736

internal class Z<K> {
    konst map = HashMap<String, String>()
    inline fun compute(key: String, producer: () -> String): String {
        return map.<!INAPPLICABLE_CANDIDATE!>getOrPut<!>(key, ::<!UNSUPPORTED!>producer<!>)
    }
}
