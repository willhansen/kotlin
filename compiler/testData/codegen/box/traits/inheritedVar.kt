//KT-2206

interface A {
    var a:Int
        get() = 239
        set(konstue) {
        }
}

class B() : A

fun box() : String {
    return if (B().a == 239) "OK" else "fail"
}
