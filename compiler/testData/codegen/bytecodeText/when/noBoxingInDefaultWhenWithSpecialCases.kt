fun testInt(i: Int?) =
    when (i) {
        0 -> "zero"
        42 -> "magic"
        else -> "other"
    }

fun testLong(i: Long?) =
    when (i) {
        0L -> "zero"
        42L -> "magic"
        else -> "other"
    }

// 0 konstueOf
// 0 Integer.konstueOf
// 0 Long.konstueOf
// 0 areEqual