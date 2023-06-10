interface ClassData

fun f() = object : ClassData {
    konst someInt: Int
        get() {
            return 5
        }
}

fun box(): String{
    f()
    return "OK"
}