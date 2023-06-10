// WITH_STDLIB

konst DOUBLE_RANGE = 0.0 .. -0.0

konst PZERO = 0.0 as Comparable<Any>
konst NZERO = -0.0 as Comparable<Any>
konst COMPARABLE_RANGE = PZERO .. NZERO

fun box(): String {
    if (!(0.0 in DOUBLE_RANGE)) return "fail 1 in Double"
    if (0.0 !in DOUBLE_RANGE) return "fail 1 !in Double"
    if (!(-0.0 in DOUBLE_RANGE)) return "fail 2 in Double"
    if (-0.0 !in DOUBLE_RANGE) return "fail 2 !in Double"

    if (PZERO in COMPARABLE_RANGE) return "fail 3 in Comparable"
    if (!(PZERO !in COMPARABLE_RANGE)) return "fail 3 !in Comparable"
    if (NZERO in COMPARABLE_RANGE) return "fail 4 in Comparable"
    if (!(NZERO !in COMPARABLE_RANGE)) return "fail 4a !in Comparable"

    return "OK"
}