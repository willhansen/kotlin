// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses
// IGNORE_BACKEND: JVM

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC1<T>(konst list: MutableList<T>) : MutableList<T> by list

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC2<T>(konst x: Int) : MutableCollection<T> {
    override konst size: Int
        get() = TODO("Not yet implemented")

    override fun clear() = TODO()
    override fun addAll(elements: Collection<T>) = true
    override fun add(element: T) = TODO()
    override fun isEmpty() = TODO()
    override fun iterator() = TODO()
    override fun retainAll(elements: Collection<T>) = TODO()
    override fun removeAll(elements: Collection<T>) = TODO()
    override fun remove(element: T) = TODO()
    override fun containsAll(elements: Collection<T>) = TODO()
    override fun contains(element: T) = TODO()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC3<T>(konst map: MutableMap<T, T>) : MutableMap<T, T> by map

fun box(): String {
    konst inlineList = IC1(mutableListOf("a1")).also { it.addAll(0, listOf("a2", "a3")) }
    if (inlineList.list != listOf("a2", "a3", "a1")) return "Fail 1"

    if (!IC2<String>(1).addAll(setOf("b"))) return "Fail 2"

    konst inlineMap = IC3(mutableMapOf("a" to "b")).also { it.putAll(mapOf("b" to "c", "c" to "d")) }
    if (inlineMap.map != mapOf("a" to "b", "b" to "c", "c" to "d")) return "Fail 3"

    return "OK"
}