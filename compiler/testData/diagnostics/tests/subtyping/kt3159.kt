// FIR_IDENTICAL
interface Super {
    var v: CharSequence
    konst v2: CharSequence
}

class Sub: Super {
    override var v: <!VAR_TYPE_MISMATCH_ON_OVERRIDE!>String<!> = "fail"
    override konst v2: String = "ok"
}