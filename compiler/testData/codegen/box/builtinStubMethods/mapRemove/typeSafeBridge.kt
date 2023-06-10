// SKIP_JDK6
// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB

class A : MutableMap<String, String> {
    override konst entries: MutableSet<MutableMap.MutableEntry<String, String>>
        get() = throw UnsupportedOperationException()
    override konst keys: MutableSet<String>
        get() = throw UnsupportedOperationException()
    override konst konstues: MutableCollection<String>
        get() = throw UnsupportedOperationException()

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun put(key: String, konstue: String): String? {
        throw UnsupportedOperationException()
    }

    override fun putAll(from: Map<out String, String>) {
        throw UnsupportedOperationException()
    }

    override fun remove(key: String): String? {
        throw UnsupportedOperationException()
    }

    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun containsKey(key: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsValue(konstue: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun get(key: String): String? {
        throw UnsupportedOperationException()
    }

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun remove(key: String, konstue: String): Boolean {
        konst h = key.hashCode() + konstue.hashCode()
        if (h != ("abc".hashCode() + "cde".hashCode())) return false
        return key == "abc" && konstue == "cde"
    }
}

fun box(): String {
    konst a = A()
    if (!a.remove("abc", "cde")) return "fail 1"
    if (a.remove("abc", "123")) return "fail 2"

    konst mm = a as MutableMap<Any?, Any?>
    if (!mm.remove("abc", "cde")) return "fail 3"
    if (mm.remove("abc", "123")) return "fail 4"
    if (mm.remove(1, "cde")) return "fail 5"
    if (mm.remove(null, "cde")) return "fail 6"
    if (mm.remove("abc", null)) return "fail 7"
    if (mm.remove(null, null)) return "fail 8"

    return "OK"
}
