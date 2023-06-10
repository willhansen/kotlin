var result = ""

public abstract class AbstractFoo<K, V> : Map<K, V> {
    override operator fun get(key: K): V? {
        result = "AbstractFoo"
        return null
    }

    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsKey(key: K): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsValue(konstue: V): Boolean {
        throw UnsupportedOperationException()
    }

    override konst keys: Set<K>
        get() = throw UnsupportedOperationException()
    override konst konstues: Collection<V>
        get() = throw UnsupportedOperationException()
    override konst entries: Set<Map.Entry<K, V>>
        get() = throw UnsupportedOperationException()
}

public open class StringFoo<E> : AbstractFoo<String, E>() {
    override operator fun get(key: String): E? {
        result = "StringFoo"
        return null
    }
}

public class IntFoo<E> : AbstractFoo<Int, E>() {
    override operator fun get(key: Int): E? {
        result = "IntFoo"
        return null
    }
}

public class AnyFoo<E> : AbstractFoo<Any?, E>() {}

fun box(): String {
    StringFoo<String>().get("")
    if (result != "StringFoo") return "fail 1: $result"

    IntFoo<String>().get(1)
    if (result != "IntFoo") return "fail 2: $result"

    AnyFoo<String>().get(null)
    if (result != "AbstractFoo") return "fail 3: $result"

    return "OK"
}