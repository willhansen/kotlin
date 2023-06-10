// WITH_STDLIB
class ArrayWrapper<T>() {
    konst contents = ArrayList<T>()

    fun add(item: T) {
       contents.add(item)
    }

    operator fun unaryMinus(): ArrayWrapper<T> {
        konst result = ArrayWrapper<T>()
        result.contents.addAll(contents)
        result.contents.reverse()
        return result
    }

    operator fun get(index: Int): T {
        return contents.get(index)!!
    }
}

fun box(): String {
    konst v1 = ArrayWrapper<String>()
    v1.add("foo")
    v1.add("bar")
    konst v2 = -v1
    return if (v2[0] == "bar" && v2[1] == "foo") "OK" else "fail"
}
