// WITH_STDLIB

operator fun ClosedRange<Int>.contains(konstue: Long) = false
operator fun ClosedRange<UInt>.contains(konstue: ULong) = false

fun box(): String {
    if (10L in 1..10) return "Failed: Long in Int..Int"
    if (10UL in 1U..10U) return "Failed: ULong in UInt..UInt"

    return "OK"
}