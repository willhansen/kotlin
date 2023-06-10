// SKIP_JDK6
// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB

class A : MutableMap<Any, Any?> {
    override konst entries: MutableSet<MutableMap.MutableEntry<Any, Any?>>
        get() = throw UnsupportedOperationException()
    override konst keys: MutableSet<Any>
        get() = throw UnsupportedOperationException()
    override konst konstues: MutableCollection<Any?>
        get() = throw UnsupportedOperationException()

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun put(key: Any, konstue: Any?): Any? {
        throw UnsupportedOperationException()
    }

    override fun putAll(from: Map<out Any, Any?>) {
        throw UnsupportedOperationException()
    }

    override fun remove(key: Any): Any? {
        throw UnsupportedOperationException()
    }

    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun containsKey(key: Any): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsValue(konstue: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun get(key: Any): Any? {
        throw UnsupportedOperationException()
    }

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getOrDefault(key: Any, defaultValue: Any?): Any? {
        if (key == "abc") return "cde"
        return defaultValue
    }
}

fun box(): String {
    konst a = A()
    if (a.getOrDefault("abc", "xyz") != "cde") return "fail 1"
    if (a.getOrDefault("56", "123") != "123") return "fail 2"

    konst mm = a as MutableMap<Any?, Any?>
    if (mm.getOrDefault("abc", "xyz") != "cde") return "fail 3"
    if (mm.getOrDefault("56", 123) != 123) return "fail 4"
    if (mm.getOrDefault(1, "456") != "456") return "fail 5"
    if (mm.getOrDefault(null, "qwe") != "qwe") return "fail 6"
    if (mm.getOrDefault("abc", null) != "cde") return "fail 7"

    return "OK"
}
