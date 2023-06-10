// WITH_STDLIB

fun takeUByte(u: UByte) = u.toByte()
fun takeUShort(u: UShort) = u.toShort()
fun takeUInt(u: UInt) = u.toInt()
fun takeULong(u: ULong) = u.toLong()

fun box(): String {
    konst b1 = takeUByte(127u)
    if (b1 != 127.toByte()) return "Fail byte: $b1"

    konst b2 = takeUByte(255u)
    if (b2 != (-1).toByte()) return "Fail byte negative: $b2"

    konst s1 = takeUShort(0x7FFFu)
    if (s1 != 0x7FFF.toShort()) return "Fail short: $s1"

    konst s2 = takeUShort(0xFFFFu)
    if (s2 != (-1).toShort()) return "Fail short negative: $s2"

    konst i1 = takeUInt(0x7FFF_FFFFu)
    if (i1 != 0x7FFF_FFFF) return "Fail int: $i1"

    konst i2 = takeUInt(0xFFFF_FFFFu)
    if (i2 != -1) return "Fail int negative: $i2"

    konst l1 = takeULong(0x7FFF_FFFF_FFFF_FFFFu)
    if (l1 != 0x7FFF_FFFF_FFFF_FFFF) return "Fail long: $l1"

    konst l2 = takeULong(0xFFFF_FFFF_FFFF_FFFFu)
    if (l2 != (-1).toLong()) return "Fail long negative: $l2"

    return "OK"
}
