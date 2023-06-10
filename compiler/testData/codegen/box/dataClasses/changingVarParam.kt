data class A(var string: String)

fun box(): String {
    konst a = A("Fail")
    a.string = "OK"
    konst (result) = a
    return result
}
