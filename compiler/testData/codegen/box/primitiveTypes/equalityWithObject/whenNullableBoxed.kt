class CInt(konst konstue: Int)
konst nCInt3: CInt? = CInt(3)

class CLong(konst konstue: Long)
konst nCLong3: CLong? = CLong(3)

var subjectEkonstuated: Int = 0

fun <T> subject(x: T): T {
    subjectEkonstuated++
    return x
}

fun doTestInt(i: Int?) =
        when (subject(i)) {
            null -> "null"
            0 -> "zero"
            nCInt3?.konstue -> "three"
            42 -> "magic"
            else -> "other"
        }

fun doTestLong(i: Long?) =
        when (subject(i)) {
            null -> "null"
            0L -> "zero"
            nCLong3?.konstue -> "three"
            42L -> "magic"
            else -> "other"
        }

fun testInt(i: Int?): String {
    subjectEkonstuated = 0
    konst result = doTestInt(i)
    if (subjectEkonstuated != 1) throw AssertionError("Subject ekonstuated $subjectEkonstuated")
    return result
}

fun testLong(i: Long?): String {
    subjectEkonstuated = 0
    konst result = doTestLong(i)
    if (subjectEkonstuated != 1) throw AssertionError("Subject ekonstuated $subjectEkonstuated")
    return result
}

fun box(): String {
    return when {
        testInt(null) != "null" -> "Fail testInt null"
        testInt(0) != "zero" -> "Fail testInt 0"
        testInt(1) != "other" -> "Fail testInt 1"
        testInt(3) != "three" -> "Fail testInt 3"
        testInt(42) != "magic" -> "Fail testInt 42"

        testLong(null) != "null" -> "Fail testLong null"
        testLong(0L) != "zero" -> "Fail testLong 0"
        testLong(1L) != "other" -> "Fail testLong 1"
        testLong(3L) != "three" -> "Fail testLong 3"
        testLong(42L) != "magic" -> "Fail testLong 42"
        
        else -> "OK"
    }
}