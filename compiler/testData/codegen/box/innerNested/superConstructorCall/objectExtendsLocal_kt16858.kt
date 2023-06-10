open class A(konst a: String, konst b: Int)

fun box(): String {
    konst o = object : A(b = 2, a = "OK") {}
    return o.a
}
