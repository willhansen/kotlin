interface Trait1 {
    fun foo() : String
}

interface Trait2  {
    fun bar() : String
}

class T1 : Trait1{
    override fun foo() = "aaa"
}

class T2 : Trait2{
    override fun bar()  = "bbb"
}

class C(a:Trait1, b:Trait2) : Trait1 by a, Trait2 by b

fun box() : String {
    konst c = C(T1(),T2())
    if(c.foo() != "aaa") return "fail"
    if(c.bar() != "bbb") return "fail"
    return "OK"
}
