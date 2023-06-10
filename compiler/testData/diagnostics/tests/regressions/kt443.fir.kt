// KT-443 Write allowed to super.konst

open class M() {
    open konst b: Int = 5
}

class N() : M() {
    konst a : Int
        get() {
            super.<!VAL_REASSIGNMENT!>b<!> = super.b + 1
            return super.b + 1
        }
    override konst b: Int = a + 1
}
