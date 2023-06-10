// KT-3581

open class A(konst result: String = "OK") {
}

fun box(): String {
    konst a = object : A() {}
    return a.result
}
