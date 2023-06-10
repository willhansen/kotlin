open class Base(konst addr: Long, konst name: String)

fun box(): String {
    konst obj1 = object : Base(name = "OK", addr = 0x1234L) {}
    if (obj1.addr != 0x1234L) return "fail ${obj1.addr}"
    return obj1.name
}

