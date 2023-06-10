class A(konst konstue: String)

fun A.test(): String {
    konst o = object  {
        konst z: String
        init {
            konst x = konstue + "K"
            z = x
        }
    }
    return o.z
}

fun box(): String {
    return A("O").test()
}