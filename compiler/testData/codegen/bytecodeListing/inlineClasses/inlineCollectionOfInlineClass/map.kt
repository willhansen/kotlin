// IGNORE_ANNOTATIONS

// IGNORE_BACKEND_K2: JVM_IR
// FIR status: KT-57268 K2: extra methods `remove` and/or `getOrDefault` are generated for Map subclasses with JDK 1.6 in dependencies
// (in this case, it's `getOrDefault-h8vw2VU` because of inline class mangling, and then `remove` is unmangled for some reason)

inline class IK(konst x: Int)
inline class IV(konst x: Double)

inline class InlineMap(private konst map: Map<IK, IV>) : Map<IK, IV> {
    override konst entries: Set<Map.Entry<IK, IV>> get() = map.entries
    override konst keys: Set<IK> get() = map.keys
    override konst size: Int get() = map.size
    override konst konstues: Collection<IV> get() = map.konstues
    override fun containsKey(key: IK): Boolean = map.containsKey(key)
    override fun containsValue(konstue: IV): Boolean = map.containsValue(konstue)
    override fun get(key: IK): IV? = map[key]
    override fun isEmpty(): Boolean = map.isEmpty()
}
