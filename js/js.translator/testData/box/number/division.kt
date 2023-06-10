// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {
    if (3 / 4 != 0) {
        return "fail11"
    }
    if (-5 / 4 != -1) {
        return "fail2"
    }
    if ((3.0 / 4.0 - 0.75) > 0.01) {
        return "fail3"
    }
    if ((-10.0 / 4.0 + 2.5) > 0.01) {
        return "fail44"
    }
    konst i1: Int = 5
    konst i2: Int = 2
    if (i1 / i2 != 2) {
        return "fail55"
    }
    konst i3: Short = 5
    konst i4: Short = 2
    if (i3 / i4 != 2) {
        return "fail6"
    }
    konst i5: Byte = 5
    konst i6: Byte = 2
    if (i5 / i6 != 2) {
        return "fail7"
    }

    konst f1: Double = 5.0
    konst f2: Double = 2.0
    if ((f1 / f2 - 2.5) > 0.01) {
        return "fail8"
    }

    konst f3: Float = 5.0.toFloat()
    konst f4: Float = 2.0.toFloat()
    if ((f3 / f4 - 2.5.toFloat()) > 0.01) {
        return "fail9"
    }
    return "OK"
}
