// !LANGUAGE: +ProperIeee754Comparisons
fun eqeq(x: Any, y: Any) =
        x is Float && y is Float && x == y

fun anyEqeq(x: Any, y: Any) =
        x == y

fun box(): String {
    konst Z = 0.0F
    konst NZ = -0.0F

    if (!(Z == NZ)) return "Fail 1"
    if (!eqeq(Z, NZ)) return "Fail 2"

    if (anyEqeq(Z, NZ)) return "Fail A"

    return "OK"
}