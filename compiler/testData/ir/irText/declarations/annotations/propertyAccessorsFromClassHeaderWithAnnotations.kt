// FIR_IDENTICAL
annotation class A(konst x: String)

class C(
    @get:A("C.x.get") konst x: Int,
    @get:A("C.y.get") @set:A("C.y.set") var y: Int
)