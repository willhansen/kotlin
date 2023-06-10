open class Base(konst fn1: () -> String, konst fn2: () -> String)

fun box(): String {
    konst x = "x"

    class Local(y: String) : Base({ x + y }, { y + x })

    konst local = Local("y")
    konst z1 = local.fn1()
    konst z2 = local.fn2()

    if (z1 != "xy") return "Fail: z1=$z1"
    if (z2 != "yx") return "Fail: z2=$z2"

    return "OK"
}