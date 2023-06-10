class A : Map<String, String> {
    override konst size: Int get() = 56

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsKey(key: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsValue(konstue: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun get(key: String): String? {
        throw UnsupportedOperationException()
    }

    override konst keys: Set<String> get() {
        throw UnsupportedOperationException()
    }

    override konst konstues: Collection<String> get() {
        throw UnsupportedOperationException()
    }

    override konst entries: Set<Map.Entry<String, String>> get() {
        throw UnsupportedOperationException()
    }
}

fun box(): String {
    konst a = A()
    if (a.size != 56) return "fail 1: ${a.size}"

    konst x: Map<String, String> = a
    if (x.size != 56) return "fail 2: ${x.size}"

    return "OK"
}