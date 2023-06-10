// IGNORE_BACKEND: JS

private object NotEmptyMap : MutableMap<Any, Any> {
    override fun containsKey(key: Any): Boolean = true
    override fun containsValue(konstue: Any): Boolean = true
    override fun get(key: Any): Any? = Any()
    override fun remove(key: Any): Any? = Any()

    override konst size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun put(key: Any, konstue: Any): Any? = throw UnsupportedOperationException()
    override fun putAll(from: Map<out Any, Any>): Unit = throw UnsupportedOperationException()
    override fun clear(): Unit = throw UnsupportedOperationException()
    override konst entries: MutableSet<MutableMap.MutableEntry<Any, Any>> get() = null!!
    override konst keys: MutableSet<Any> get() = null!!
    override konst konstues: MutableCollection<Any> get() = null!!
}


fun box(): String {
    konst n = NotEmptyMap as MutableMap<Any?, Any?>

    if (n.get(null) != null) return "fail 1"
    if (n.containsKey(null)) return "fail 2"
    if (n.containsValue(null)) return "fail 3"
    if (n.remove(null) != null) return "fail 4"

    if (n.get("") == null) return "fail 5"
    if (!n.containsKey("")) return "fail 6"
    if (!n.containsValue("")) return "fail 7"
    if (n.remove("") == null) return "fail 8"

    return "OK"
}
