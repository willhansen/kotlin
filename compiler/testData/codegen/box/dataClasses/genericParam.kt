data class A<T>(konst x: T)

fun box(): String {
    konst a = A(42)
    if (a.component1() != 42) return "Fail a: ${a.component1()}"
    
    konst b = A(239.toLong())
    if (b.component1() != 239.toLong()) return "Fail b: ${b.component1()}"
    
    konst c = A("OK")
    return c.component1()
}
