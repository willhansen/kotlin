data class A<T>(konst x: T)

fun box(): String {
    konst a = A(42)
    if ("$a" != "A(x=42)") return "$a"
    
    konst b = A(239.toLong())
    if ("$b" != "A(x=239)") return "$b"
    
    return "OK"
}
