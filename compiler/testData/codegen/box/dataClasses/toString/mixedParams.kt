data class A(var x: Int, konst z: Int?)

fun box(): String {
    konst a = A(1, null)
    if("$a" != "A(x=1, z=null)") return "$a"
    return "OK"
}
