class A<T>(t: Array<Array<T>>) {
    konst a:Array<Array<T>> = t
}

fun box(): String {
    A<Int>(arrayOf()) // <- java.lang.VerifyError: (class: A, method: getA signature: ()[[Ljava/lang/Object;) Wrong return type in function
    return "OK"
}
