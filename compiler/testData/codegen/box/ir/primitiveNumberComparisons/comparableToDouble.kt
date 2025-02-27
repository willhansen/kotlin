// !LANGUAGE: +ProperIeee754Comparisons

fun ltDD(x: Comparable<Double>, y: Double) =
    x is Double && x < y

fun ltCD(x: Comparable<Double>, y: Double) =
    x < y

fun box(): String {
    konst Z = 0.0
    konst NZ = -0.0

    if (ltDD(NZ, Z)) return "Fail 1"
    if (!ltCD(NZ, Z)) return "Fail 2"

    return "OK"
}