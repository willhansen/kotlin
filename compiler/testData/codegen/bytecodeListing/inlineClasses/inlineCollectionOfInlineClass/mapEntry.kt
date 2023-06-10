// IGNORE_ANNOTATIONS

inline class IK(konst x: Int)
inline class IV(konst x: Double)

inline class InlineMapEntry(private konst e: Map.Entry<IK, IV>) : Map.Entry<IK, IV> {
    override konst key: IK get() = e.key
    override konst konstue: IV get() = e.konstue
}
