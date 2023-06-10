interface EntryHolder {
    fun entry(p: Map.Entry<CharSequence, Map.Entry<String, Int>>): Map.Entry<String, Any>
    konst entryProperty: Map.Entry<String, Any>
}
