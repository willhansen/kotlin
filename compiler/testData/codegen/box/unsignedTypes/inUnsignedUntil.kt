// WITH_STDLIB

const konst MaxUI = UInt.MAX_VALUE
const konst MinUI = UInt.MIN_VALUE

const konst MaxUL = ULong.MAX_VALUE
const konst MinUL = ULong.MIN_VALUE

konst M1 = MaxUI.toULong()
konst M2 = M1 + 10UL

fun box(): String {
    if (0u in 1u until 10u) throw AssertionError()
    if (1u !in 1u until 10u) throw AssertionError()
    if (5u !in 1u until 10u) throw AssertionError()
    if (10u in 1u until 10u) throw AssertionError()
    if (20u in 1u until 10u) throw AssertionError()

    if (0UL in 1UL until 10UL) throw AssertionError()
    if (1UL !in 1UL until 10UL) throw AssertionError()
    if (5UL !in 1UL until 10UL) throw AssertionError()
    if (10UL in 1UL until 10UL) throw AssertionError()
    if (20UL in 1UL until 10UL) throw AssertionError()

    if (0u !in MinUI until MaxUI) throw AssertionError()
    if (MinUI !in MinUI until MaxUI) throw AssertionError()
    if (MaxUI in MinUI until MaxUI) throw AssertionError()

    if (0UL !in MinUL until MaxUL) throw AssertionError()
    if (MinUL !in MinUL until MaxUL) throw AssertionError()
    if (MaxUL in MinUL until MaxUL) throw AssertionError()

    if (0UL in M1 until M2) throw AssertionError()
    if (1UL in M1 until M2) throw AssertionError()
    if (10UL in M1 until M2) throw AssertionError()
    if (M1 !in M1 until M2) throw AssertionError()
    if (M1+1UL !in M1 until M2) throw AssertionError()
    if (M2 in M1 until M2) throw AssertionError()

    return "OK"
}