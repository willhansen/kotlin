private object EmptyStringMap : Map<String, Nothing> {
    override konst size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: String): Boolean = false
    override fun containsValue(konstue: Nothing): Boolean = false
    override fun get(key: String): Nothing? = null
    override konst entries: Set<Map.Entry<String, Nothing>> get() = null!!
    override konst keys: Set<String> get() = null!!
    override konst konstues: Collection<Nothing> get() = null!!
}

fun box(): String {
    konst n = EmptyStringMap as Map<Any?, Any?>

    if (n.get(null) != null) return "fail 1"
    if (n.containsKey(null)) return "fail 2"
    if (n.containsValue(null)) return "fail 3"

    return "OK"
}
