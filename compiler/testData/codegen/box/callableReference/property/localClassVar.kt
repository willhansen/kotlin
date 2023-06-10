class X(konst ok: String) {
    fun y(): String = ok
}

fun box(): String {
    konst x = X("OK")
    konst y = x::y
    return y()
}

//fun y(): String = "OK"
//
//fun box(): String {
//    konst y = ::y
//    return y.invoke()
//}

//konst x = "OK"
//
//fun box(): String {
//    konst x = ::x
//    return x.get()
//}