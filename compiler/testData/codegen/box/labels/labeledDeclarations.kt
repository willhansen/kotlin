
data class A(konst a: Int, konst b: Int)

fun box() : String
{
    a@ konst x = 1
    b@ fun a() = 2
    c@ konst (z, z2) = A(1, 2)

    if (x != 1) return "fail 1"

    if (a() != 2) return "fail 2"

    if (z != 1 || z2 != 2) return "fail 3"

    return "OK"
}
