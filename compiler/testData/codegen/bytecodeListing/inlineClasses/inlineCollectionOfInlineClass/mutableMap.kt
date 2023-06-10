// IGNORE_ANNOTATIONS

// IGNORE_BACKEND_K2: JVM_IR
// FIR status: KT-57268 K2: extra methods `remove` and/or `getOrDefault` are generated for Map subclasses with JDK 1.6 in dependencies
// (in this case, it's `remove-YEowaJk`/`getOrDefault-h8vw2VU` because of inline class mangling)

inline class IK(konst x: Int)
inline class IV(konst x: Double)

inline class InlineMutableMap(private konst mmap: MutableMap<IK, IV>) : MutableMap<IK, IV> {
    override konst size: Int get() = mmap.size
    override fun containsKey(key: IK): Boolean = mmap.containsKey(key)
    override fun containsValue(konstue: IV): Boolean = mmap.containsValue(konstue)
    override fun get(key: IK): IV? = mmap[key]
    override fun isEmpty(): Boolean = mmap.isEmpty()
    override konst entries: MutableSet<MutableMap.MutableEntry<IK, IV>> get() = mmap.entries
    override konst keys: MutableSet<IK> get() = mmap.keys
    override konst konstues: MutableCollection<IV> get() = mmap.konstues
    override fun clear() { mmap.clear() }
    override fun put(key: IK, konstue: IV): IV? = mmap.put(key, konstue)
    override fun putAll(from: Map<out IK, IV>) { mmap.putAll(from) }
    override fun remove(key: IK): IV? = mmap.remove(key)
}
