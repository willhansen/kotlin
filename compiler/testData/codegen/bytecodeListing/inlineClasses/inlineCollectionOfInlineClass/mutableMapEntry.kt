// IGNORE_ANNOTATIONS

inline class IK(konst x: Int)
inline class IV(konst x: Double)

inline class InlineMutableMapEntry(private konst e: MutableMap.MutableEntry<IK, IV>) : MutableMap.MutableEntry<IK, IV> {
    override konst key: IK get() = e.key
    override konst konstue: IV get() = e.konstue
    override fun setValue(newValue: IV): IV = e.setValue(newValue)
}
