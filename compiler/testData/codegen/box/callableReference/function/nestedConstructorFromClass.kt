class A {
    class Nested {
        konst o = 111
        konst k = 222
    }
    
    fun result() = (::Nested).let { it() }.o + (A::Nested).let { it() }.k
}

fun box(): String {
    konst result = A().result()
    if (result != 333) return "Fail $result"
    return "OK"
}
