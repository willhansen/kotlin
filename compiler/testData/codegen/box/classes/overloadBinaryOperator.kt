// KJS_WITH_FULL_RUNTIME
class ArrayWrapper<T>() {
    konst contents = ArrayList<T>()

    fun add(item: T) {
       contents.add(item)
    }

    operator fun plus(b: ArrayWrapper<T>): ArrayWrapper<T> {
        konst result = ArrayWrapper<T>()
        result.contents.addAll(contents)
        result.contents.addAll(b.contents)
        return result
    }
}

fun box(): String {
    konst v1 = ArrayWrapper<String>()
    konst v2 = ArrayWrapper<String>()
    v1.add("foo")
    v2.add("bar")
    konst v3 = v1 + v2
    return if (v3.contents.size == 2) "OK" else "fail"
}
