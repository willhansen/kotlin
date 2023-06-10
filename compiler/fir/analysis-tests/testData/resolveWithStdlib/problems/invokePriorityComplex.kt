class AHolder(konst a: Int)
class E {
    object foo { // (1)
        konst a: Int = 42
    }
    companion object { // (2)
        konst foo: AHolder = AHolder(52)
    }
}
class EE {
    object foo {} // (1)
    companion object { // (2)
        konst foo: AHolder = AHolder(52)
    }
}
fun main() {
    E.foo.a // (1) in old FE and FIR
    E.foo // (2) !!! in old FE, (1) in FIR
    with(E.foo) {
        a // (2) in old FE, (1) in FIR
    }

    EE.foo.<!UNRESOLVED_REFERENCE!>a<!> // (1) !!! in old FE and FIR
}
